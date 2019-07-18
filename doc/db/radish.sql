-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: radish
-- ------------------------------------------------------
-- Server version	5.1.73

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `agent`
--

DROP TABLE IF EXISTS `agent`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agent`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键id',
    `agent_name`  varchar(125) DEFAULT '' COMMENT '客户端名称',
    `agent_ip`    varchar(125) DEFAULT '' COMMENT '客户端IP',
    `agent_port`  int(11)      DEFAULT '0' COMMENT '客户端rest rpc端口',
    `admin`       varchar(64)  DEFAULT '' COMMENT '客户端管理员',
    `admin_email` varchar(125) DEFAULT '' COMMENT '客户端管理员邮箱',
    `admin_phone` varchar(125) DEFAULT '' COMMENT '客户端管理员手机号',
    `reg_time`    datetime     DEFAULT NULL COMMENT '客户端注册时间',
    `stat`        tinyint(2)   DEFAULT '0' COMMENT 'Agent状态',
    `network`     varchar(32) COMMENT '日志访问模式',
    `netty_port`  int(11) COMMENT '启动netty监听的端口',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_group`
--

DROP TABLE IF EXISTS `agent_group`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agent_group`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键id',
    `group_name`  varchar(125) DEFAULT '' COMMENT '组名',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_group_ref`
--

DROP TABLE IF EXISTS `agent_group_ref`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agent_group_ref`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键id',
    `agent_id`       int(11) DEFAULT '-1' COMMENT '客户端ID',
    `agent_group_id` int(11) DEFAULT '-1' COMMENT '客户端组ID',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_handler`
--

DROP TABLE IF EXISTS `agent_handler`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agent_handler`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键id',
    `agent_id`    int(11)      DEFAULT '-1' COMMENT '客户端ID',
    `handler`     varchar(64)  DEFAULT '' COMMENT '客户端Handler',
    `description` varchar(125) DEFAULT '' COMMENT '客户端Handler描述',
    `enable`      tinyint(2)   DEFAULT '1' COMMENT '是否启用 1启用 0 禁用',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_info`
--

DROP TABLE IF EXISTS `job_info`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_info`
(
    `id`                    int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一主键id',
    `parent_job_id`         varchar(125)  DEFAULT NULL COMMENT '父任务ID, 多个用逗号分割',
    `job_name`              varchar(256)  DEFAULT NULL COMMENT 'job名称',
    `crontab`               varchar(32)   DEFAULT NULL COMMENT '定时策略',
    `handler_type`          varchar(32)   DEFAULT NULL COMMENT 'job类型, shell, java, Python etc',
    `handler_fail_strategy` varchar(32)   DEFAULT NULL COMMENT '处理器失败策略',
    `admin`                 varchar(64)   DEFAULT NULL COMMENT 'Job负责人',
    `admin_email`           varchar(125)  DEFAULT NULL COMMENT '负责人邮箱',
    `admin_phone`           varchar(125)  DEFAULT NULL COMMENT '负责人手机号',
    `executor_handlers`     varchar(512)  DEFAULT NULL COMMENT '执行处理器',
    `cmd`                   text COMMENT '执行脚本',
    `params`                varchar(512)  DEFAULT NULL COMMENT '附加参数',
    `param_file_path`       varchar(512)  DEFAULT NULL COMMENT '附加参数附件',
    `dist_type`             varchar(32)   DEFAULT NULL COMMENT '分片策略',
    `dist_rule`             varchar(1024) DEFAULT NULL COMMENT '分片规则',
    `create_time`           datetime      DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime      DEFAULT NULL COMMENT '更新时间',
    `priority`              tinyint(2)    DEFAULT '0' COMMENT '任务优先级 0 - 9',
    `enable`                tinyint(2)    DEFAULT '1' COMMENT '是否启用, 1: 启用, 0:禁用',
    `user_id`               int(11)       DEFAULT NULL COMMENT '创建用户ID',
    `expired`               varchar(32)   DEFAULT NULL COMMENT '任务生成的事件过期移除的时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2018-08-24 16:46:15

DROP TABLE IF EXISTS `job_event`;
CREATE TABLE `job_event`
(
    `event_id`          varchar(32) NOT NULL COMMENT '事件id',
    `parent_event_id`   varchar(32) COMMENT '父事件id',
    `job_id`            int(11)     NOT NULL COMMENT 'job 任务id',
    `parent_job_id`     varchar(125)  DEFAULT NULL COMMENT '父job 任务id',
    `group_id`          varchar(32)   DEFAULT NULL COMMENT '事件组id',
    `parent_group_id`   varchar(32)   DEFAULT NULL COMMENT '父事件组id',
    `executor_handlers` varchar(512)  DEFAULT NULL COMMENT '执行处理器',
    `handler_type`      varchar(32)   DEFAULT NULL COMMENT 'job类型, shell, java, Python etc',
    `cmd`               text          DEFAULT NULL COMMENT '执行脚本',
    `params`            varchar(512)  DEFAULT NULL COMMENT '附加参数',
    `param_file_path`   varchar(512)  DEFAULT NULL COMMENT '附加参数附件',
    `dist_type`         varchar(32)   DEFAULT NULL COMMENT '分片类型',
    `event_rule`        varchar(1024) DEFAULT NULL COMMENT '事件分片规则',
    `stat`              varchar(32)   DEFAULT NULL COMMENT '状态',
    `handler_agent_id`  int(11)       DEFAULT NULL COMMENT '执行处理的Agent ID',
    `handler_log_path`  varchar(128) COMMENT 'agent执行事件的日志路径',
    `priority`          tinyint(2)    DEFAULT '0' COMMENT '任务优先级 0 - 9',
    `retry_count`       smallint(3)   DEFAULT '0' COMMENT '重试次数',
    `create_time`       datetime      DEFAULT NULL COMMENT '创建时间',
    `trigger_time`       datetime      DEFAULT NULL COMMENT '抢占时间',
    `handle_time`       datetime      DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`event_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `job_event_his`;
CREATE TABLE `job_event_his`
(
    `event_id`          varchar(32) NOT NULL COMMENT '事件id',
    `parent_event_id`   varchar(32) COMMENT '父事件id',
    `job_id`            int(11)     NOT NULL COMMENT 'job 任务id',
    `parent_job_id`     varchar(125)  DEFAULT NULL COMMENT '父job 任务id',
    `group_id`          varchar(32)   DEFAULT NULL COMMENT '事件组id',
    `parent_group_id`   varchar(32)   DEFAULT NULL COMMENT '父事件组id',
    `executor_handlers` varchar(512)  DEFAULT NULL COMMENT '执行处理器',
    `handler_type`      varchar(32)   DEFAULT NULL COMMENT 'job类型, shell, java, Python etc',
    `cmd`               text          DEFAULT NULL COMMENT '执行脚本',
    `params`            varchar(512)  DEFAULT NULL COMMENT '附加参数',
    `param_file_path`   varchar(512)  DEFAULT NULL COMMENT '附加参数附件',
    `dist_type`         varchar(32)   DEFAULT NULL COMMENT '分片类型',
    `event_rule`        varchar(1024) DEFAULT NULL COMMENT '事件分片规则',
    `stat`              varchar(32)   DEFAULT NULL COMMENT '状态',
    `handler_agent_id`  int(11)       DEFAULT NULL COMMENT '执行处理的Agent ID',
    `handler_log_path`  varchar(128) COMMENT 'agent执行事件的日志路径',
    `priority`          tinyint(2)    DEFAULT '0' COMMENT '任务优先级 0 - 9',
    `retry_count`       smallint(3)   DEFAULT '0' COMMENT '重试次数',
    `create_time`       datetime      DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`event_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- ----------------------------
--  Table structure for `app_info`
-- ----------------------------
DROP TABLE IF EXISTS `app_info`;
CREATE TABLE `app_info`
(
    `app_id`   varchar(32) NOT NULL COMMENT '应用id',
    `app_name` varchar(128) DEFAULT NULL COMMENT '应用名称',
    `domain`   varchar(128) DEFAULT NULL COMMENT '域名',
    `user_id`  int(11)      DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`app_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


-- ----------------------------
--  Table structure for `job_app_ref`
-- ----------------------------
DROP TABLE IF EXISTS `job_app_ref`;
CREATE TABLE `job_app_ref`
(
    `id`     varchar(32) NOT NULL,
    `job_id` varchar(32) DEFAULT NULL,
    `app_id` varchar(32) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


-- ----------------------------
--  Table structure for `app_kind`
-- ----------------------------
DROP TABLE IF EXISTS `app_kind`;
CREATE TABLE `app_kind`
(
    `id`     varchar(32) NOT NULL COMMENT '主键',
    `app_id` varchar(32) DEFAULT NULL COMMENT '应用ID',
    `kind`   varchar(32) DEFAULT NULL COMMENT '分类标签',
    `handlers` varchar(512) DEFAULT NULL COMMENT '处理器',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


-- ----------------------------
--  Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uname`    varchar(32) DEFAULT NULL COMMENT '用户名',
    `password` varchar(32) DEFAULT NULL COMMENT '密码',
    `enable`   int(1)      DEFAULT NULL COMMENT '是否可用',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;


-- ----------------------------
--  Table structure for `user_agent_group`
-- ----------------------------
DROP TABLE IF EXISTS `user_agent_group`;
CREATE TABLE `user_agent_group`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`  int(11) DEFAULT NULL COMMENT '用户ID',
    `group_id` int(11) DEFAULT NULL COMMENT '机组ID',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;

-- ----------------------------
--  Table structure for `job_scheduler`
-- ----------------------------
DROP TABLE IF EXISTS `job_scheduler`;
CREATE TABLE `job_scheduler`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `job_id`         int(11)     DEFAULT NULL COMMENT '任务ID',
    `nid`            int(11)     DEFAULT NULL COMMENT '任务运行的节点ID',
    `running_status` varchar(10) DEFAULT NULL COMMENT '任务运行状态',
    `prev_fire_time` datetime    DEFAULT NULL COMMENT '上次执行时间',
    `next_fire_time` datetime    DEFAULT NULL COMMENT '下次执行时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8;

-- Initialize administrator
INSERT INTO user(id, uname, password, enable)
VALUES (1, 'admin', 'admin', 1);
COMMIT;