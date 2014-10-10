package com.loic.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
/**
 * HttpUtils
 * <ul>
 * <strong>Http get, you can also use {@link HttpCache}</strong>
 * <li>{@link #httpGet(HttpRequest)} http get synchronous</li>
 * <li>{@link #httpGet(String)} http get synchronous</li>
 * <li>{@link #httpGetString(String)} http get synchronous, response is String</li>
 * <li>{@link #httpGet(HttpRequest, HttpListener)} http get asynchronous</li>
 * <li>{@link #httpGet(String, HttpListener)} http get asynchronous</li>
 * </ul>
 * <ul>
 * <strong>Http post</strong>
 * <li>{@link #httpPost(HttpRequest)}</li>
 * <li>{@link #httpPost(String)}</li>
 * <li>{@link #httpPostString(String)}</li>
 * <li>{@link #httpPostString(String, Map)}</li>
 * </ul>
 * <ul>
 * <strong>Http params</strong>
 * <li>{@link #getUrlWithParas(String, Map)}</li>
 * <li>{@link #getUrlWithValueEncodeParas(String, Map)}</li>
 * <li>{@link #joinParas(Map)}</li>
 * <li>{@link #joinParasWithEncodedValue(Map)}</li>
 * <li>{@link #appendParaToUrl(String, String, String)}</li>
 * <li>{@link #parseGmtTime(String)}</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-12
 */
public class HttpUtils 
{
    /** url and para separator **/
    public static final String URL_AND_PARA_SEPARATOR = "?";
    /** parameters separator **/
    public static final String PARAMETERS_SEPARATOR   = "&";
    /** paths separator **/
    public static final String PATHS_SEPARATOR        = "/";
    /** equal sign **/
    public static final String EQUAL_SIGN             = "=";


    public static String httpGet(String url) throws FailException
    {
    	String retVal = null;
        if(!url.isEmpty())
        {
        	HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse response = client.execute(httpGet);
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) 
                {
                    HttpEntity entity = response.getEntity();
                    retVal = EntityUtils.toString(entity);
                } else
                	throw new FailException("Can't connect to the server, status:" + status+ " recevied.");
                httpGet.abort();
            } catch (UnsupportedEncodingException e) {
            	throw new FailException(e.getMessage());
    		} catch (ClientProtocolException e) {
    			throw new FailException(e.getMessage());
    		} catch (IOException e) {
    			throw new FailException(e.getMessage());
    		} finally {
                client.getConnectionManager().shutdown();
            }
        }
        return retVal;
    }

    /**
     * http post
     * <ul>
     * <li>use gzip compression default</li>
     * <li>use bufferedReader to improve the reading speed</li>
     * </ul>
     * 
     * @param httpUrl
     * @param paras
     * @return the response of the url, if null represents http error
     */
    public static String httpPost(String url, List<NameValuePair> data) throws FailException
    {
    	String retVal = null;
        if(!url.isEmpty())
        {
        	HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            try 
            {
                httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
                HttpResponse response = client.execute(httpPost);
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) 
                {
                    HttpEntity entity = response.getEntity();
                    retVal = EntityUtils.toString(entity);
                }
                else
                	throw new FailException("Can't connect to the server, status:" + status+ " recevied.");
                httpPost.abort();
            } catch (UnsupportedEncodingException e) {
            	throw new FailException(e.getMessage());
    		} catch (ClientProtocolException e) {
    			throw new FailException(e.getMessage());
    		} catch (IOException e) {
    			throw new FailException(e.getMessage());
    		} finally {
                client.getConnectionManager().shutdown();
            }
        }
        return retVal;
    }

    /**
     * HttpListener, can do something before or after HttpGet
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-15
     */
    public static abstract class HttpListener {

        /**
         * Runs on the UI thread before httpGet.<br/>
         * <ul>
         * <li>this can be null if you not want to do something</li>
         * </ul>
         */
        protected void onPreGet() {}

        /**
         * Runs on the UI thread after httpGet. The httpResponse is returned by httpGet.
         * <ul>
         * <li>this can be null if you not want to do something</li>
         * </ul>
         * 
         * @param httpResponse get by the url
         */
        protected void onPostGet(HttpResponse httpResponse) {}
    }
}
