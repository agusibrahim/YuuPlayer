package id.agusibrahim.yuuplayersample;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.pm.*;
import android.content.*;
import java.util.*;
import java.text.*;
import id.agusibrahim.yuuplayer.YuuPlayer;
import id.agusibrahim.yuuplayer.YuuPlayerFullscreen;

public class MainActivity extends Activity implements YuuPlayer.OnReceivedData
{
	private View loadbtn;
	private YuuPlayer yuu;
	private Button playbtn,btninfo,downbtn;
	private CheckBox mutetoggler;
	private View stopbtn;
	private EditText tvidid;
	private TextView curtime,txtstate;
	private SeekBar volctrl;
	List<YuuPlayer.Video> myvideo=new ArrayList<YuuPlayer.Video>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		loadbtn=findViewById(R.id.mainButton1);
		txtstate=(TextView) findViewById(R.id.mainTextView2);
		playbtn=(Button) findViewById(R.id.mainButtonplay);
		stopbtn=findViewById(R.id.mainButtonstop);
		downbtn=(Button) findViewById(R.id.mainButton2);
		volctrl=(SeekBar) findViewById(R.id.mainSeekBar1);
		tvidid=(EditText) findViewById(R.id.mainEditText1);
		curtime=(TextView) findViewById(R.id.mainTextView1);
		btninfo=(Button) findViewById(R.id.mainButtoninfo);
		mutetoggler=(CheckBox) findViewById(R.id.mainCheckmute);
		downbtn.setEnabled(false);
		yuu=(YuuPlayer)findViewById(R.id.mainYuuPlayer);
		//yuu.setAudioVideoBlockEnable(true);
		loadbtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					if(tvidid.getText().length()!=11){
						tvidid.setError("Invalid Video ID");
						return;
					}
					yuu.setVideo(tvidid.getText().toString().trim());
				}
			});
		playbtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					if(!yuu.isPlaying) yuu.playVideo();
					else yuu.pauseVideo();
				}
			});
		stopbtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					yuu.stopVideo();
				}
			});
		mutetoggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton p1, boolean p2) {
					yuu.setMute(p2);
				}
			});
		btninfo.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					yuu.getDuration(MainActivity.this);
				}
			});
		downbtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					if(myvideo.size()==0){
						Toast.makeText(MainActivity.this, "Video URL not Ready, play video first!",0).show();
						return;
					}
					yuu.pauseVideo();
					String[] vids=new String[myvideo.size()];
					for(int i=0;i<myvideo.size();i++){
						YuuPlayer.Video v=myvideo.get(i);
						vids[i]=(v.isAudioOnly?"Audio":"Video")+" ("+v.readableSize+")";
					}
					AlertDialog.Builder dlg=new AlertDialog.Builder(MainActivity.this);
					dlg.setTitle("Download");
					dlg.setItems(vids, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface p1, int p2) {
								download_(myvideo.get(p2).url);
							}
						});
					dlg.show();
				}
			});
		yuu.setOnReadyListener(new YuuPlayer.OnReadyListener(){
				@Override
				public void oReady() {
					yuu.setVolume(50);
					volctrl.setProgress(50);
					Toast.makeText(MainActivity.this, "Ready",0).show();
				}
			});
		yuu.setOnVideoURLReadyListener(new YuuPlayer.OnVideoURLReadyListener(){
				@Override
				public void onVideoURLReady(YuuPlayer.Video vid) {
					boolean isExists=false;
					for(YuuPlayer.Video v:myvideo){
						if(v.size==vid.size) isExists=true;
					}
					if(!isExists) myvideo.add(vid);
					downbtn.setEnabled(true);
				}
			});
		yuu.setOnErrorListener(new YuuPlayer.OnErrorListener(){
				@Override
				public void onError(String error) {
					// Error codes are here https://developers.google.com/youtube/iframe_api_reference#onError
					curtime.setText("Player Error: "+error);
				}
			});
		
		volctrl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
					yuu.setVolume(volctrl.getProgress());
				}

				@Override
				public void onStartTrackingTouch(SeekBar p1) {
					// TODO: Implement this method
				}

				@Override
				public void onStopTrackingTouch(SeekBar p1) {
					// TODO: Implement this method
				}
			});
		yuu.setOnPlayerProgressListener(new YuuPlayer.OnPlayerProgressListener(){
				@Override
				public void onPlayerProgress(float currentTime) {
					curtime.setText(""+toHHMMSS(currentTime));
				}
			});
		yuu.setOnStateChangeListener(new YuuPlayer.OnStateChangeListener(){
				@Override
				public void onStateChange(int state) {
					String textState=""+state;
					if(state!=YuuPlayer.STATE_PLAYING){
						yuu.unregisterProgressUpdate();
						loadbtn.setEnabled(true);
						playbtn.setText("Play");
					}
					switch(state){
						case YuuPlayer.STATE_UNSTARTED:
							textState="UNSTARTED";
							break;
						case YuuPlayer.STATE_CUED:
							textState="CUED";
							break;
						case YuuPlayer.STATE_ENDED:
							textState="ENDED";
							break;
						case YuuPlayer.STATE_PAUSED:
							textState="PAUSED";
							break;
						case YuuPlayer.STATE_PLAYING:
							textState="PLAYING";
							playbtn.setText("Pause");
							yuu.registerProgressUpdate(500);
							loadbtn.setEnabled(false);
							break;
						case YuuPlayer.STATE_BUFFERING:
							textState="BUFFERING";
							break;
					}
					txtstate.setText(textState);
				}
			});
    }
	
	@Override
	public void receivedData(int tag, String data) {
		switch(tag){
			case YuuPlayer.TAG_DURATION:
				Toast.makeText(this, "Video Duration is "+toHHMMSS(Float.parseFloat(data)),0).show();
				break;
		}
	}
	private String toHHMMSS(float c){
		Date d = new Date((int)c * 1000);
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss"); // HH for 0-23
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(d);
	}
	
	private void download_(String url){
		DownloadManager dmgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(android.net.Uri.parse(url));
		request.setAllowedNetworkTypes(
			DownloadManager.Request.NETWORK_WIFI
			| DownloadManager.Request.NETWORK_MOBILE)
			.setAllowedOverRoaming(false).setTitle("Download")
			.setDescription("Downloading Video...")
			.setDestinationInExternalPublicDir("/Download/", System.currentTimeMillis()+".mp4");
		dmgr.enqueue(request);
	}
}
