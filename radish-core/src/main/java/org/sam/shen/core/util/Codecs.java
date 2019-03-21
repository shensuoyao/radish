package org.sam.shen.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;

public class Codecs {
	
	private static SecureRandom random = new SecureRandom();
	
	public enum Encoding{
		UTF8("utf-8"), GBK("gbk");
		private String value;
		private Encoding(String value){
			this.value = value;
		}
		public String toString(){
			return value;
		}
	}

	public static Encoding DEFAULT_URL_ENCODING = Encoding.UTF8;
	
	public static final String ALGORITHM_DES = "DES";
	
	public static final String ALGORITHM_SHA1 = "SHA-1";
	
	public static final String ALGORITHM_MD5 = "MD5";
	
	public static final int HASH_INTERATIONS = 1024;
	
	public static final int SALT_SIZE = 8;
	
	/**
	 * 默认算法为DES
	 * 
	 * 可替换为以下任意一种算法，同时key值的size相应改变:
	 * DES          		key size must be equal to 56
	 * DESede(TripleDES) 	key size must be equal to 112 or 168
	 * AES          		key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available
	 * Blowfish     		key size must be multiple of 8, and can only range from 32 to 448 (inclusive)
	 * RC2          		key size must be between 40 and 1024 bits
	 * RC4(ARCFOUR) 		key size must be between 40 and 1024 bits
	 */
	public static final String DEFAULT_ALGORITHM = "DES";
	
	public static final String DEFAULT_DES_KEY = "COM.CHINACAREER.NINO.DEFAULT_KEY";
	
	
	/**
	 * 对文件进行md5散列.
	 */
	public static byte[] md5Encode(InputStream input) throws IOException {
		return digest(input, ALGORITHM_MD5);
	}
	
	/**
	 * 对string进行md5散列.
	 */
	public static byte[] md5Encode(String input) throws IOException {
		return digest(input.getBytes(Encoding.UTF8.value), ALGORITHM_MD5, null, 1);
	}
	
	/**
	 * 对输入字符串进行sha1散列.
	 */
	public static byte[] sha1Encode(byte[] input) {
		return digest(input, ALGORITHM_SHA1, null, 1);
	}

	public static byte[] sha1Encode(byte[] input, byte[] salt) {
		return digest(input, ALGORITHM_SHA1, salt, 1);
	}

	public static byte[] sha1Encode(byte[] input, byte[] salt, int iterations) {
		return digest(input, ALGORITHM_SHA1, salt, iterations);
	}

	/**
	 * 对文件进行sha1散列.
	 */
	public static byte[] sha1Encode(InputStream input) throws IOException {
		return digest(input, ALGORITHM_SHA1);
	}

	/**
	 * Hex编码.
	 */
	public static String hexEncode(byte[] input) {
		return Hex.encodeHexString(input);
	}

	/**
	 * Hex解码.
	 */
	public static byte[] hexDecode(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalStateException("Hex Decoder exception", e);
		}
	}

	/**
	 * Base64编码.
	 */
	public static String base64Encode(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}

	/**
	 * Base64编码, URL安全(将Base64中的URL非法字符如+,/=转为其他字符, 见RFC3548).
	 */
	public static String base64UrlSafeEncode(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
	}

	/**
	 * Base64解码.
	 */
	public static byte[] base64Decode(String input) {
		return Base64.decodeBase64(input);
	}

	/**
	 * URL 编码, Encode默认为UTF-8. 
	 */
	public static String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, DEFAULT_URL_ENCODING.toString());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

	/**
	 * URL 解码, Encode默认为UTF-8. 
	 */
	public static String urlDecode(String input) {
		try {
			return URLDecoder.decode(input, DEFAULT_URL_ENCODING.toString());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}
	
	/**
	 * 生成随机的Byte[]作为salt.
	 * 
	 * @param numBytes byte数组的大小
	 */
	public static byte[] generateSalt(int numBytes) {
		Validate.isTrue(numBytes > 0, "numBytes argument must be a positive integer (1 or larger)", numBytes);

		byte[] bytes = new byte[numBytes];
		random.nextBytes(bytes);
		return bytes;
	}
	
	public static String charEscape(char c){
		Number n =(int)c;
		String encode = Integer.toHexString(n.intValue());
		while(encode.length()<4){
			encode = "0" + encode;
		}
		encode = "\\u" + encode;
		return encode;
	}

	/**
	 * Html 转码.
	 */
	public static String htmlEscape(String html) {
		return StringEscapeUtils.escapeHtml4(html);
	}

	/**
	 * Html 解码.
	 */
	public static String htmlUnescape(String htmlEscaped) {
		return StringEscapeUtils.unescapeHtml4(htmlEscaped);
	}

	/**
	 * Xml 转码.
	 */
	@SuppressWarnings("deprecation")
	public static String xmlEscape(String xml) {
		return StringEscapeUtils.escapeXml(xml);
	}

	/**
	 * Xml 解码.
	 */
	public static String xmlUnescape(String xmlEscaped) {
		return StringEscapeUtils.unescapeXml(xmlEscaped);
	}
	
	/**
	 * 生成密钥
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws Exception
	 */
	public static String initKey() throws NoSuchAlgorithmException {
		return initKey(null);
	}
	
	/**
	 * 生成密钥
	 * 
	 * @param seed
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws Exception
	 */
	public static String initKey(String seed) throws NoSuchAlgorithmException{
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		if (seed != null) {
			//secureRandom = new SecureRandom(base64Decode(seed));
			secureRandom.setSeed(base64Decode(seed));
		} else {
			secureRandom = new SecureRandom();
		}
		KeyGenerator kg = KeyGenerator.getInstance(DEFAULT_ALGORITHM);
		kg.init(secureRandom);

		SecretKey secretKey = kg.generateKey();

		return base64Encode(secretKey.getEncoded());
	}
	
	/**
	 * 转换密钥<br>
	 * 
	 * @param key
	 * @return
	 * @throws InvalidKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws Exception
	 */
	private static Key toKey(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey secretKey = null;
		if(ALGORITHM_DES.equals(DEFAULT_ALGORITHM)){
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);
			secretKey = keyFactory.generateSecret(dks);
		}else{
			secretKey = new SecretKeySpec(key, DEFAULT_ALGORITHM);
		}
		return secretKey;
	}
	
	/**
	 * 加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchPaddingException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws Exception
	 */
	public static String encrypt(byte[] data, String key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		Key k = toKey(base64Decode(key));
		Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, k);
		return base64Encode(cipher.doFinal(data));
	}
	
	public static String encrypt(String data, String key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		return encrypt(data.getBytes(), key);
	}
	
	public static String encrypt(String data) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		String key = initKey(DEFAULT_DES_KEY);
		return encrypt(data.getBytes(), key);
	}
	
	/**
	 * 解密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchPaddingException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws Exception
	 */
	public static String decrypt(byte[] data, String key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Key k = toKey(base64Decode(key));
		Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, k);
		return new String(cipher.doFinal(data));
	}
	
	public static String decrypt(String data, String key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return decrypt(base64Decode(data), key);
	}
	
	public static String decrypt(String data) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String key = initKey(DEFAULT_DES_KEY);
		return decrypt(base64Decode(data), key);
	}	
	
	/**
	 * 对字符串进行散列, 支持md5与sha1算法.
	 */
	private static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);

			if (salt != null) {
				digest.update(salt);
			}

			byte[] result = digest.digest(input);

			for (int i = 1; i < iterations; i++) {
				digest.reset();
				result = digest.digest(result);
			}
			return result;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static byte[] digest(InputStream input, String algorithm) throws IOException {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			int bufferLength = 8 * 1024;
			byte[] buffer = new byte[bufferLength];
			int read = input.read(buffer, 0, bufferLength);

			while (read > -1) {
				messageDigest.update(buffer, 0, read);
				read = input.read(buffer, 0, bufferLength);
			}

			return messageDigest.digest();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		byte[] salt = Codecs.generateSalt(Codecs.SALT_SIZE);
		String _salt = Codecs.hexEncode(salt);
		System.out.println("salt : " + _salt);
		String passwd = "123456";
		byte[] plainPassword = passwd.getBytes();
		byte[] hashPassword = Codecs.sha1Encode(plainPassword, salt, Codecs.HASH_INTERATIONS);
		String _passwd = Codecs.hexEncode(hashPassword);
		System.out.println("password : " + _passwd);
	}
}
