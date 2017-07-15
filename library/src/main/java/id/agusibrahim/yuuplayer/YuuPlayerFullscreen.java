package id.agusibrahim.yuuplayer;
import android.app.Activity;
import android.os.*;
import android.content.*;

public class YuuPlayerFullscreen extends Activity
{
	public static String PARAM_VIDEO_ID="agusibrahim.yuu.videoid";
	public static String PARAM_VIDEO_QUALITY="agusibrahim.yuu.videoquality";
	public static String PARAM_AUTOPLAY="agusibrahim.yuu.autoplay";
	public static String PARAM_SEEK="agusibrahim.yuu.videoseek";
	YuuPlayer yuu;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		yuu=new YuuPlayer(this);
		Intent intent=getIntent();
		yuu.init_videoid=intent.getStringExtra(PARAM_VIDEO_ID);
		yuu.init_quality=intent.getStringExtra(PARAM_VIDEO_QUALITY);
		yuu.init_seek=intent.getIntExtra(PARAM_SEEK, 0);
		yuu.init_autoplay=intent.getBooleanExtra(PARAM_AUTOPLAY, false);
		setContentView(yuu);
		
	}
}
