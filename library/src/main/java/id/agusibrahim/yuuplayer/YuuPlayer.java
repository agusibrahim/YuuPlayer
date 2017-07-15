package id.agusibrahim.yuuplayer;
import android.webkit.WebView;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.MotionEvent;
import android.webkit.WebResourceResponse;
import android.util.Log;
import android.app.Dialog;
import id.agusibrahim.yuuplayer.YuuPlayer.*;
import android.webkit.JavascriptInterface;
import android.os.Looper;
import android.os.Handler;
import android.content.res.TypedArray;

public class YuuPlayer extends WebView
{
	private int frameHeight;
	public static boolean isPlaying=false;
	public boolean isAudioVideoBlock=false;
	public boolean isVideoBlock=false;
	private YuuPlayer.OnStateChangeListener cbstate;
	private YuuPlayer.OnReadyListener cbready;
	public static final int STATE_UNSTARTED=-1;
	public static final int STATE_ENDED=0;
	public static final int STATE_PLAYING=1;
	public static final int STATE_PAUSED=2;
	public static final int STATE_BUFFERING=3;
	public static final int STATE_CUED=5;
	public static final int TAG_CURRENT_TIME=11;
	public static final int TAG_DURATION=12;
	public static final int TAG_VOLUME=13;
	public static final String QUALITY_SMALL="small";
	public static final String QUALITY_MEDIUM="medium";
	public static final String QUALITY_LARGE="large";
	public static final String QUALITY_HD720="hd720";
	public static final String QUALITY_HD1080="hd1080";
	public static final String QUALITY_HIGHRES="highres";
	public static final String QUALITY_DEFAULT="default";
	
	private YuuPlayer.OnReceivedData cbrecvdata;
	private YuuPlayer.OnPlayerProgressListener cbprog;
	private YuuPlayer.OnErrorListener cberr;
	private YuuPlayer.OnVideoURLReadyListener cbvidurl;
	private int init_volume=60;
	public String init_quality="default";
	public String init_videoid="";
	public int init_seek=0;
	public boolean init_autoplay=false;
	
	public YuuPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }
    public YuuPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.YuuValue);
		init_seek=ta.getInt(R.styleable.YuuValue_seek,0);
		init_videoid=ta.getString(R.styleable.YuuValue_video_id);
		init_quality=ta.getString(R.styleable.YuuValue_video_quality);
		init_volume=ta.getInt(R.styleable.YuuValue_volume, 60);
		init_autoplay=ta.getBoolean(R.styleable.YuuValue_autoplay, false);
		initView();
    }
    public YuuPlayer(Context context) {
        super(context);
        initView();
    }
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		parentHeight=(int)convertPixelsToDp(parentHeight, getContext())+1;
		frameHeight=parentHeight;
		//Toast.makeText(getContext(), ""+parentHeight,0).show();
		loadData(contentBuilder(parentHeight, ""), "text/html; charset=utf-8", "UTF-8");
		this.setMeasuredDimension(parentWidth, parentHeight);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	public void setVideo(String videoid){
		loadData( contentBuilder(frameHeight, videoid), "text/html", "UTF-8");
	}
	public void setMute(boolean state){
		loadUrl("javascript:player."+(state?"mute":"unMute")+"()");
	}
	public void setLoop(boolean loop){
		loadUrl("javascript:player.setLoop("+loop+")");
	}
	public void playVideo(){
		loadUrl("javascript:player.playVideo()");
	}
	public void pauseVideo(){
		loadUrl("javascript:player.pauseVideo()");
	}
	public void stopVideo(){
		loadUrl("javascript:player.stopVideo()");
	}
	public void setVolume(int vol){
		loadUrl("javascript:player.setVolume("+vol+")");
	}
	public void setQuality(String quality){
		loadUrl("javascript:player.setPlaybackQuality('"+quality+"')");
	}
	public void setAudioVideoBlockEnable(boolean v){
		isAudioVideoBlock=v;
	}
	public void setVideoBlockEnable(boolean v){
		isVideoBlock=v;
	}
	public void registerProgressUpdate(int interval){
		loadUrl("javascript:function updateProg(){callme.setProgress(player.getCurrentTime()),window.progto=setTimeout(updateProg,"+(interval<100?100:interval)+")}updateProg();");
	}
	public void unregisterProgressUpdate(){
		loadUrl("javascript:clearTimeout(window.progto);");
	}
	public void getCurrentTime(OnReceivedData x){
		loadUrl("javascript:callme.setData("+TAG_CURRENT_TIME+", player.getCurrentTime())");
		cbrecvdata=x;
	}
	public void getDuration(OnReceivedData x){
		loadUrl("javascript:callme.setData("+TAG_DURATION+", player.getDuration())");
		cbrecvdata=x;
	}
	public void getVolume(OnReceivedData x){
		loadUrl("javascript:callme.setData("+TAG_VOLUME+", player.getVolume())");
		cbrecvdata=x;
	}
	public void setOnStateChangeListener(OnStateChangeListener x){
		cbstate=x;
	}
	public void setOnReadyListener(OnReadyListener x){
		cbready=x;
	}
	public void setOnErrorListener(OnErrorListener x){
		cberr=x;
	}
	public void setOnPlayerProgressListener(OnPlayerProgressListener x){
		cbprog=x;
	}
	public void setOnVideoURLReadyListener(OnVideoURLReadyListener x){
		cbvidurl=x;
	}
	
	private void initView() {
		//Toast.makeText(getContext(), "init",0).show();
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		getSettings().setJavaScriptEnabled(true);
		addJavascriptInterface(new JsFace(), "callme");
		setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return (event.getAction() == MotionEvent.ACTION_MOVE);
				}
			});
		setWebViewClient(new WebViewClient(){
				@Override
				public void onPageStarted(android.webkit.WebView view, java.lang.String url, android.graphics.Bitmap favicon) {
					if(!url.matches(".*meta name.*")){
						stopLoading();
						Log.d("webb", "STOP "+url);
					}
				}
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, final String url) {
					if(url.matches(".*googlevideo.com/videoplayback.*")){
						new Handler(Looper.getMainLooper()).post(new Runnable(){
								@Override
								public void run(){
									Log.d("webbb", "VIDEO URL READY: " + url.replaceAll("&range=[\\d-]*&","&"));
									if(cbvidurl!=null) cbvidurl.onVideoURLReady(new Video(url));
								}
							});
						if(isAudioVideoBlock)
							return new WebResourceResponse("text/css", "UTF-8", null);
						else{
							if(isVideoBlock&&url.contains("&mime=video")){
								return new WebResourceResponse("text/css", "UTF-8", null);
							}
							return super.shouldInterceptRequest(view, url);
						}
					}
					if (!url.matches(".*(iframe_api|timedtext|embed|\\.jpg|\\.png|www-widget|jsbin|meta|cssbin|js/bg).*")) { // add other specific resources..
						Log.d("webb", "LOAD DENIED: " + url);
						return new WebResourceResponse("text/css","UTF-8",null);
					} else {
						Log.d("webb", "LOAD OK: " + url);
						return super.shouldInterceptRequest(view, url);
					}
				}
			});
		//setWebChromeClient(new android.webkit.WebChromeClient());
	}
	public float convertPixelsToDp(float px, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return dp;
	}
	class JsFace {
		@JavascriptInterface
		public void setState(final int state) {
			if(state==STATE_PLAYING) isPlaying=true;
			else isPlaying=false;
			new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run(){
						if(cbstate!=null) cbstate.onStateChange(state);
					}
				});
			Log.d("webstate", "STATE "+state);
		}
		@JavascriptInterface
		public void setReady() {
			new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run(){
						if(cbready!=null) cbready.oReady();
					}
				});
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
					@Override
					public void run(){
						if(init_autoplay) playVideo();
					}
				},1000);
			
			Log.d("webstate", "READY ");
		}
		@JavascriptInterface
		public void setError(final String e) {
			new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run(){
						if(cberr!=null) cberr.onError(e);
					}
				});
			Log.d("webstate", "Error ");
		}
		@JavascriptInterface
		public void setData(final int tag, final String data){
			new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run(){
						cbrecvdata.receivedData(tag, data);
					}
				});
		}
		@JavascriptInterface
		public void setProgress(final String ct) {
			new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run() {
						if (cbprog != null) {
							try {
								cbprog.onPlayerProgress(Float.parseFloat(ct));
							} catch (Exception e) {
								cbprog.onPlayerProgress(0);
							}
						}
					}
				});
		}
	}
	private String readableFileSize(long size) {
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new java.text.DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	private String contentBuilder(int height, String videoid){
		String sb = "<!DOCTYPE html>" +
			"<html>" +
			"  <head>" +
			"      <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, minimal-ui\" />" +
			"        <title>Hello Agus Ibrahim</title>" +
			"    </head>" +
			"  <body>" +
			"    <div id=\"player\" style=\"width: 100%;height: "+height+"px;\"></div>" +
			"<style type=\"text/css\">" +
			"* {" +
			"padding: 0;" +
			"margin: 0;" +
			"}" +
			"html, body {" +
			"overflow: hidden;" +
			"touch-action: none;" +
			"background: #000;" +
			"-ms-touch-action: none;" +
			"}" +
			"div {" +
			"    background: #000;" +
			"touch-action-delay: none;" +
			"touch-action: none;" +
			"pointer-events: none !important;"+
			"-ms-touch-action: none;" +
			"}" +
			"    </style>" +
			"    <script>" +
			"      var tag = document.createElement('script');" +
			"      tag.src = \"https://www.youtube.com/iframe_api\";" +
			"      var firstScriptTag = document.getElementsByTagName('script')[0];" +
			"      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);" +
			"      function onYouTubeIframeAPIReady() {" +
			"        window.player = new YT.Player('player', {" +
			"          height: null," +
			"          width: null," +
			"          videoId: '"+videoid+"'," +
			"          events: {" +
			"            'onReady': onPlayerReady," +
			"            'onError': onPlayerError," +
			"            'onStateChange': onPlayerStateChange" +
			"          }" +
			"        });" +
			"      }" +
			"	   function onPlayerReady(event) {" +
			"          player.setVolume("+init_volume+");" +
			"          player.loadVideoById('"+(videoid.length()>10?videoid:init_videoid)+"', "+init_seek+", '"+init_quality+"');player.stopVideo();" +
			"          callme.setReady();" +
			"      }" +
			"	   function onPlayerError(event) {" +
			"          callme.setError(event.data);" +
			"      }" +
			"      function onPlayerStateChange(event) {" +
			"          callme.setState(event.data);" +
			"        }" +
			"	   document.addEventListener('click',function(t){t.preventDefault(),t.stopPropagation()},!0);"+
			"    </script>" +
			"  </body>" +
			"</html>";
		return sb;
	}
	public interface OnStateChangeListener{
		void onStateChange(int state);
	}
	public interface OnReadyListener{
		void oReady();
	}
	public interface OnErrorListener{
		void onError(String error);
	}
	public interface OnReceivedData{
		void receivedData(int tag, String data);
	}
	public interface OnPlayerProgressListener{
		void onPlayerProgress(float currentTime);
	}
	public interface OnVideoURLReadyListener{
		void onVideoURLReady(Video vid);
	}
	public class Video{
		public String url;
		public boolean isAudioOnly;
		public long size;
		public String readableSize;
		public Video(String s){
			String ss=s.replaceAll("&range=[\\d-]*&","&");
			url=ss;
			isAudioOnly=ss.contains("mime=audio");
			size=Long.parseLong( ss.split("&clen=")[1].split("&")[0]);
			readableSize=readableFileSize(size);
		}
	}
}
