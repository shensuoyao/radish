package org.sam.shen.core.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.sam.shen.core.model.Resp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RestRequest {
	private static Logger logger = LoggerFactory.getLogger(RestRequest.class);

	public static final String APPLICATION_JSON_VALUE = "application/json";
	
	private static RestTemplate restTemplate;
	
	static {
		// 长连接保持30秒
		PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30,
		        TimeUnit.SECONDS);
		// 总连接数
		pollingConnectionManager.setMaxTotal(100);
		// 同路由的并发数
		pollingConnectionManager.setDefaultMaxPerRoute(100);

		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		httpClientBuilder.setConnectionManager(pollingConnectionManager);
		// 重试次数，默认是3次，没有开启
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
		// 保持长连接配置，需要在头添加Keep-Alive
		httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());

		List<Header> headers = new ArrayList<>();
		headers.add(new BasicHeader("User-Agent",
		        "Mozilla/5.0 (X11; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0"));
		headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate,br"));
		headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headers.add(new BasicHeader("Connection", "Keep-Alive"));

		httpClientBuilder.setDefaultHeaders(headers);

		HttpClient httpClient = httpClientBuilder.build();

		// httpClient连接配置，底层是配置RequestConfig
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
		        httpClient);
		// 连接超时
		clientHttpRequestFactory.setConnectTimeout(5000);
		// 数据读取超时时间，即SocketTimeout
		clientHttpRequestFactory.setReadTimeout(5000);
		// 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
		clientHttpRequestFactory.setConnectionRequestTimeout(200);
		// 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
		// clientHttpRequestFactory.setBufferRequestBody(false);

		restTemplate = new RestTemplate(clientHttpRequestFactory);
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

		if(logger.isInfoEnabled()) {
			logger.info("RestRequest 初始化完成 ...");
		}
	}
	
	public RestRequest() {
		super();
	}
	
	public static void put(String url, Object request) throws Exception {
		try {
			restTemplate.put(url, request);
		} catch(RestClientException e) {
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Resp<Object> post(String url, Object request) throws Exception {
		return restTemplate.postForObject(url, request, Resp.class);
	}
	
	public static <T> Resp<T> get(String url, Class<T> clazz, String... paramKvs) throws Exception {
		Map<String, Object> uriVariables = Maps.newHashMap();
		if(paramKvs != null) {
			for(String p : paramKvs) {
				String[] kvs = p.split("=");
				uriVariables.put(kvs[0], kvs[1]);
			}
		}
		return get(url, uriVariables, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Resp<T> get(String url, Map<String, Object> uriVariables, Class<T> clazz) throws Exception {
		if(null != uriVariables && uriVariables.size() > 0) {
			url = url.concat("?");
			List<String> variables = Lists.newArrayList();
			for(String key : uriVariables.keySet()) {
				variables.add(key.concat("={").concat(key).concat("}"));
			}
			url = url.concat(Joiner.on("&").join(variables));
		}
		
		Resp<Object> resp = restTemplate.getForObject(url, Resp.class, uriVariables);
		
		return new Resp<T>(JSON.parseObject(resp.toJsonBody(), clazz));
	}
	
}
