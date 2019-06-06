package org.sam.shen.scheduing.cluster;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.sam.shen.scheduing.cluster.ClusterCnxManager.Message;
import org.sam.shen.scheduing.cluster.ClusterPeer.ClusterServer;
import org.sam.shen.scheduing.cluster.ClusterPeer.NodeState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FastLeaderElection implements Election {

	ClusterCnxManager manager;

	ClusterPeer self;
	Messenger messenger;

	// 推荐的leader ID
	int proposedLeader;

	// 推荐的 rhid
	int proposedRhid;

	// 推荐的选举次数
	long proposedEpoch;

	volatile long logicalclock; /* Election instance */

	// 发送队列
	LinkedBlockingQueue<ToSend> sendqueue;

	// 接收队列
	LinkedBlockingQueue<Notification> recvqueue;

	volatile boolean stop;

	public FastLeaderElection(ClusterPeer self, ClusterCnxManager manager) {
		this.stop = false;
		this.manager = manager;
		starter(self, manager);
	}

	private void starter(ClusterPeer self, ClusterCnxManager manager) {
		this.self = self;
		proposedLeader = -1;
		proposedRhid = -1;

		sendqueue = new LinkedBlockingQueue<>();
		recvqueue = new LinkedBlockingQueue<>();
		this.messenger = new Messenger(manager);
	}

	@Override
	public void shutdown() {
		stop = true;
		log.debug("Shutting down connection manager");
		manager.halt();
		log.debug("Shutting down messenger");
		messenger.halt();
		log.debug("FLE is down");
	}

	/**
	 * 将要发送给其他选举者的信息
	 * 
	 * @author suoyao
	 * @date 2019年3月4日 下午5:55:57
	 * 
	 */
	static public class ToSend {
		static enum mType {
			crequest, challenge, notification, ack
		}

		ToSend(mType type, int leader, int rhid, long electionEpoch, NodeState state, int nid) {
			this.leader = leader;
			this.rhid = rhid;
			this.electionEpoch = electionEpoch;
			this.state = state;
			this.nid = nid;
		}

		int leader;
		int rhid;
		long electionEpoch;
		ClusterPeer.NodeState state;
		// 推荐者的节点 ID
		int nid;
	}

	/**
	 * 通知其他选举者的信息
	 * 
	 * @author suoyao
	 * @date 2019年3月4日 下午4:05:53
	 * 
	 */
	static public class Notification {

		// 建议的leader ID
		int leader;

		// 建议的leader rhid
		int rhid;

		// 选举轮次
		long electionEpoch;

		// 当前发送者的状态
		ClusterPeer.NodeState state;

		// 发送者的 node ID
		int nid;

		@Override
		public String toString() {
			return new String("(message format version), " + leader + " (n.leader), 0x" + Long.toHexString(rhid)
			        + " (n.rhid), 0x" + Long.toHexString(electionEpoch) + " (n.round), " + state + " (n.state), " + nid
			        + " (n.nid), 0x");
		}
	}

	/**
	 * 创建一个发送的信息包
	 * 
	 * @author suoyao
	 * @date 上午11:46:22
	 * @param state
	 * @param leader
	 * @param rhid
	 * @param electionEpoch
	 * @return
	 */
	static ByteBuffer buildMsg(int state, int leader, int rhid, long electionEpoch) {
		byte requestBytes[] = new byte[40];
		ByteBuffer requestBuffer = ByteBuffer.wrap(requestBytes);
		requestBuffer.clear();
		requestBuffer.putInt(state);
		requestBuffer.putInt(leader);
		requestBuffer.putInt(rhid);
		requestBuffer.putLong(electionEpoch);
		return requestBuffer;
	}

	/**
	 * 获取新投票
	 * 
	 * @author suoyao
	 * @date 下午3:12:38
	 * @return
	 */
	synchronized Vote getVote() {
		return new Vote(proposedLeader, proposedRhid, proposedEpoch);
	}

	/**
	 * 1. 负责从ClusterCnxManager 获取并处理信息 2. 负责通过ClusterCnxManager 发送信息到其他节点服务器
	 * 
	 * @author suoyao
	 * @date 2019年3月5日 上午10:13:33
	 * 
	 */
	protected class Messenger {
		/**
		 * 接收并处理来自ClusterCnxManager 的信息
		 * 
		 * @author suoyao
		 * @date 2019年3月5日 上午10:14:51
		 * 
		 */
		class WorkerReceiver implements Runnable {
			volatile boolean stop;
			ClusterCnxManager manager;

			WorkerReceiver(ClusterCnxManager manager) {
				this.stop = false;
				this.manager = manager;
			}

			public void run() {
			    log.info("WorkerReceiver start");
				Message response;
				while (!stop) {
					// Sleeps on receive
					try {
						response = manager.pollRecvQueue(3000, TimeUnit.MILLISECONDS);
						if (response == null) {
							continue;
						}

						if (!self.getView().containsKey(response.nid)) {
							Vote current = self.getCurrentVote();
							ToSend notmsg = new ToSend(ToSend.mType.notification, current.getNid(), current.getRhid(),
							        current.getElectionEpoch(), self.getNodeState(), response.nid);

							sendqueue.offer(notmsg);
						} else {
							// 接收新的消息
							if (log.isDebugEnabled()) {
								log.debug("Receive new notification message. My id = " + self.getMyId());
							}

							// 初始化一个通知
							Notification n = new Notification();

							// State of peer that sent this message
							ClusterPeer.NodeState ackstate;
							switch (response.buffer.getInt()) {
							case 0:
								ackstate = ClusterPeer.NodeState.LOOKING;
								break;
							case 1:
								ackstate = ClusterPeer.NodeState.FOLLOWING;
								break;
							case 2:
								ackstate = ClusterPeer.NodeState.LEADING;
								break;
							case 3:
								ackstate = ClusterPeer.NodeState.OBSERVING;
								break;
							default:
								continue;
							}

							n.leader = response.buffer.getInt();
							n.rhid = response.buffer.getInt();
							n.electionEpoch = response.buffer.getLong();
							n.state = ackstate;
							n.nid = response.nid;

							/*
							 * 如果当前节点服务器的状态是 looking，则发送推荐的leader投票
							 */
							if (self.getNodeState() == ClusterPeer.NodeState.LOOKING) {
								recvqueue.offer(n);

								/*
								 * 如果发送信息的节点服务器的状态也是LOOKING， 并且它的投票次数落后于当前节点服务器的逻辑次数， 则向发送信息的服务器发送一个投票通知
								 */
								if ((ackstate == ClusterPeer.NodeState.LOOKING) && (n.electionEpoch < logicalclock)) {
									Vote v = getVote();
									ToSend notmsg = new ToSend(ToSend.mType.notification, v.getNid(), v.getRhid(),
									        logicalclock, self.getNodeState(), response.nid);
									sendqueue.offer(notmsg);
								}
							} else {
								/*
								 * 如果当前节点服务器的状态不是LOOKING， 但是发送ack确认的节点服务器状态是LOOKING， 则返回可信的leader
								 */
								Vote current = self.getCurrentVote();
								if (ackstate == ClusterPeer.NodeState.LOOKING) {
									if (log.isDebugEnabled()) {
										log.debug("Sending new notification. My id =  " + self.getMyId() + " recipient="
										        + response.nid + " rhid=0x" + Long.toHexString(current.getRhid())
										        + " leader=" + current.getNid());
									}
									ToSend notmsg = new ToSend(ToSend.mType.notification, current.getNid(),
									        current.getRhid(), current.getElectionEpoch(), self.getNodeState(),
									        response.nid);
									sendqueue.offer(notmsg);
								}
							}
						}
					} catch (InterruptedException e) {
						log.error("Interrupted Exception while waiting for new message" + e.toString());
						Thread.currentThread().interrupt();
					}
				}
				log.info("WorkerReceiver is down");
			}
		}

		/**
		 * 将发送信息放入发送队列中
		 * 
		 * @author suoyao
		 * @date 2019年3月5日 上午11:32:31
		 * 
		 */
		class WorkerSender implements Runnable {
			volatile boolean stop;
			ClusterCnxManager manager;

			WorkerSender(ClusterCnxManager manager) {
				this.stop = false;
				this.manager = manager;
			}

			public void run() {
				log.info("WorkerSender start");
				while (!stop) {
					try {
						ToSend m = sendqueue.poll(3000, TimeUnit.MILLISECONDS);
						if (m == null) {
							continue;
						}
						process(m);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
				log.info("WorkerSender is down");
			}

			/*
			 * 处理新的需要发送的信息
			 */
			void process(ToSend m) {
				ByteBuffer requestBuffer = buildMsg(m.state.ordinal(), m.leader, m.rhid, m.electionEpoch);
				manager.toSend(m.nid, requestBuffer);
			}
		}

		/*
		 * 判断发送和接收队列是否都为空
		 */
		public boolean queueEmpty() {
			return (sendqueue.isEmpty() || recvqueue.isEmpty());
		}

		WorkerSender ws;
		WorkerReceiver wr;

		/*
		 * 构造函数
		 */
		Messenger(ClusterCnxManager manager) {

			this.ws = new WorkerSender(manager);

			Thread t = new Thread(this.ws, "WorkerSender[myid=" + self.getMyId() + "]");
			t.setDaemon(true);
			t.start();

			this.wr = new WorkerReceiver(manager);

			t = new Thread(this.wr, "WorkerReceiver[myid=" + self.getMyId() + "]");
			t.setDaemon(true);
			t.start();
		}

		/*
		 * 停止workerSender 和 workerReciver
		 */
		void halt() {
			this.ws.stop = true;
			this.wr.stop = true;
		}
	}

	// 一旦确认选举结束后，延迟多少时间开始处理选票
	final static int finalizeWait = 200;

	// 最大的通知延迟时间
	final static int maxNotificationInterval = 60000;

	/**
	 * 更新推荐投票信息
	 * 
	 * @author suoyao
	 * @date 下午3:45:41
	 * @param leader
	 * @param rhid
	 * @param epoch
	 */
	synchronized void updateProposal(int leader, int rhid, long epoch) {
		proposedLeader = leader;
		proposedRhid = rhid;
		proposedEpoch = epoch;
	}

	/**
	 * 将自己的投票发送给所有的节点服务器
	 */
	private void sendNotifications() {
		for (ClusterServer server : self.getView().values()) {
			int nid = server.nid;
//			if(self.getMyId() == nid) {
//				continue;
//			}

			ToSend notmsg = new ToSend(ToSend.mType.notification, proposedLeader, proposedRhid, logicalclock,
			        ClusterPeer.NodeState.LOOKING, nid);
			if (log.isDebugEnabled()) {
				log.debug("Sending Notification: " + proposedLeader + " (n.leader), 0x" + Long.toHexString(proposedRhid)
				        + " (n.rhid), 0x" + Long.toHexString(logicalclock) + " (n.round), " + nid + " (recipient), "
				        + self.getMyId() + " (myid), 0x" + Long.toHexString(proposedEpoch));
			}
			sendqueue.offer(notmsg);
		}
	}

	/**
	 * 比较pair(nid, rhid) 是否胜出当前节点服务器
	 * 
	 * @author suoyao
	 * @date 下午4:16:07
	 * @param newNid
	 * @param newRhid
	 * @param newEpoch
	 * @param curNid
	 * @param curRhid
	 * @param curEpoch
	 * @return
	 */
	protected boolean totalOrderPredicate(long newNid, long newRhid, long newEpoch, long curNid, long curRhid,
	        long curEpoch) {
		log.debug("nid: " + newNid + ", proposed nid: " + curNid + ", rhid: 0x" + Long.toHexString(newRhid)
		        + ", proposed rhid: 0x" + Long.toHexString(curRhid));

		/*
		 * 先比较rhid，然后再比较nid
		 */
		return ((newEpoch > curEpoch)
		        || ((newEpoch == curEpoch) && ((newRhid > curRhid) || ((newRhid == curRhid) && (newNid > curNid)))));
	}

	/**
	 * 判断投票是否超过半数
	 *
	 * @author suoyao
	 * @date 下午5:07:09
	 * @param votes
	 * @param vote
	 * @return
	 */
	protected boolean termPredicate(HashMap<Integer, Vote> votes, Vote vote) {
		HashSet<Integer> set = new HashSet<>();
		for (Map.Entry<Integer, Vote> entry : votes.entrySet()) {
			if (vote.equals(entry.getValue())) {
				set.add(entry.getKey());
			}
		}
		int half = self.getView().size() / 2;
		return set.size() > half;
	}

	private void leaveInstance(Vote v) {
		log.info("About to leave FLE instance: leader=" + v.getNid() + ", rhid=0x" + Long.toHexString(v.getRhid())
		        + ", my id=" + self.getMyId() + ", my state=" + self.getNodeState());
		recvqueue.clear();
	}

	/**
	 * 检查是否已经选举出了一个leader
	 * @author suoyao
	 * @date 下午4:02:37
	 * @param recv
	 * @param ooe
	 * @param n
	 * @return
	 */
	protected boolean ooePredicate(HashMap<Integer, Vote> recv, HashMap<Integer, Vote> ooe, Notification n) {

		return (termPredicate(recv, new Vote(n.leader, n.rhid, n.electionEpoch, n.state))
		        && checkLeader(ooe, n.leader, n.electionEpoch));

	}

	protected boolean checkLeader(HashMap<Integer, Vote> votes, int leader, long electionEpoch) {

		boolean predicate = true;

		/*
		 * 如果某个节点认为自己是leader，那么它的状态必须是leading状态
		 */
		if (leader != self.getMyId()) {
			if (votes.get(leader) == null)
				predicate = false;
			else if (votes.get(leader).getState() != NodeState.LEADING)
				predicate = false;
		} else if (logicalclock != electionEpoch) {
			predicate = false;
		}

		return predicate;
	}

	@Override
	public Vote lookForLeader() throws InterruptedException {
		if (self.start_fle == 0) {
			self.start_fle = System.currentTimeMillis();
		}
		
		// 收到的投票集
		HashMap<Integer, Vote> recvset = new HashMap<>();

		HashMap<Integer, Vote> outofelection = new HashMap<>();

		int notTimeout = finalizeWait;

		synchronized (this) {
			// 开始投票选举，逻辑时钟加一
			logicalclock++;
			updateProposal(self.getMyId(), self.updateRhid(), logicalclock);
		}

		log.info("New election. My id =  " + self.getMyId() + ", proposed rhid=0x" + Long.toHexString(proposedRhid)
		        + "My Address=" + self.getMyLeaderAddr().getAddress());
		sendNotifications();

		/*
		 * 循环处理通知信息，直到选出一个新的leader为止
		 */
		while ((self.getNodeState() == NodeState.LOOKING) && (!stop)) {

			Notification n = recvqueue.poll(notTimeout, TimeUnit.MILLISECONDS);

			/*
			 * 如果没有收到足够的通知，就发送更多的通知道其他机器，
			 * 否则就处理新的接收到的通知
			 */
			if (n == null) {
				if (manager.haveDelivered()) {
					sendNotifications();
				} else {
					manager.connectAll();
				}

				int tmpTimeOut = notTimeout * 2;
				notTimeout = (tmpTimeOut < maxNotificationInterval ? tmpTimeOut : maxNotificationInterval);
				log.info("Notification time out: " + notTimeout);
			} else if (self.getView().containsKey(n.nid)) {
				/*
				 * 只处理投票副本视图中的节点服务器发来的通知
				 */
				switch (n.state) {
                    case LOOKING:
                        // If notification > current, replace and send messages out
                        if (n.electionEpoch > logicalclock) {
                            logicalclock = n.electionEpoch;
                            recvset.clear();
                            if (totalOrderPredicate(n.leader, n.rhid, n.electionEpoch, proposedLeader, proposedRhid,
                                    logicalclock)) {
                                // 更新成通知的投票信息
                                updateProposal(n.leader, n.rhid, n.electionEpoch);
                            } else {
                                updateProposal(self.getMyId(), self.updateRhid(), logicalclock);
                            }
                            sendNotifications();
                        } else if (n.electionEpoch < logicalclock) {
                            // 接收到的推荐轮次小于当前节点服务器的选举轮次，则不做处理.
                            break;
                        } else if (totalOrderPredicate(n.leader, n.rhid, n.electionEpoch, proposedLeader, proposedRhid,
                                proposedEpoch)) {
                            updateProposal(n.leader, n.rhid, n.electionEpoch);
                            sendNotifications();
                        }
                        // 将投票加入到接收选票集中
                        recvset.put(n.nid, new Vote(n.leader, n.rhid, n.electionEpoch));

                        if (termPredicate(recvset, new Vote(proposedLeader, proposedRhid, logicalclock))) {
                            // 验证推荐的leader是否有新的变化
                            while ((n = recvqueue.poll(finalizeWait, TimeUnit.MILLISECONDS)) != null) {
                                if (totalOrderPredicate(n.leader, n.nid, n.electionEpoch, proposedLeader, proposedRhid,
                                        proposedEpoch)) {
                                    recvqueue.put(n);
                                    break;
                                }
                            }

                            // 没有任何改变的通知
                            if (n == null) {
                                self.setNodeState(
                                        (proposedLeader == self.getMyId()) ? NodeState.LEADING : NodeState.FOLLOWING);

                                Vote endVote = new Vote(proposedLeader, proposedRhid, proposedEpoch);
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }
                        break;
                    case OBSERVING:
                        log.debug("Notification from observer: " + n.nid);
                        break;
                    case FOLLOWING:
                    case LEADING:
                        // 比较投票轮次是否一致
                        if (n.electionEpoch == logicalclock) {
                            recvset.put(n.nid, new Vote(n.leader, n.rhid, n.electionEpoch));

                            if (ooePredicate(recvset, outofelection, n)) {
                                self.setNodeState((n.leader == self.getMyId()) ? NodeState.LEADING : NodeState.FOLLOWING);

                                Vote endVote = new Vote(n.leader, n.rhid, n.electionEpoch);
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }

                        /*
                         * 加入集群leader之前，先检查是否有超过一半的节点服务器跟随当前的leader
                         */
                        outofelection.put(n.nid, new Vote(n.leader, n.rhid, n.electionEpoch, n.state));

                        if (ooePredicate(outofelection, outofelection, n)) {
                            synchronized (this) {
                                logicalclock = n.electionEpoch;
                                self.setNodeState((n.leader == self.getMyId()) ? NodeState.LEADING : NodeState.FOLLOWING);
                            }
                            Vote endVote = new Vote(n.leader, n.rhid, n.electionEpoch);
                            leaveInstance(endVote);
                            return endVote;
                        }
                        break;
                    default:
                        log.warn("Notification state unrecognized: {} (n.state), {} (n.sid)", n.state, n.nid);
                        break;
				}
			} else {
				log.warn("Ignoring notification from non-cluster member " + n.nid);
			}
		}
		return null;
	}

}
