package com.android.presentation.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.media.MediaPlayer;
import android.view.SurfaceView;
import android.view.View.*;
import android.view.*;
import android.widget.*;
import android.view.WindowManager.*;
import android.util.Log;
import java.io.File;

import com.android.presentation.app.PresentationActivity;
import com.android.presentation.app.R;
import com.softwinner.SystemMix;

public class NfsDialog extends Dialog implements OnClickListener
{
  private final String TAG = "NfsDialog.java";
  Context context;
  Button mOk;
  EditText nfsUrl;

  private static int NFS_FLAGS = 32768; // NFS mount flags
  private static String NFS_OPTS = "nolock,addr="; // NFS mount options
  private static String NFS_TYPE = "nfs"; // NFS mount type
  public final static String NFS_SPLIT = "/";

  public NfsDialog(Context context)
  {
    super(context);
    // TODO Auto-generated constructor stub
    this.context = context;

    init();
  }

  public NfsDialog(Context context, int theme)
  {
    super(context, theme);
    this.context = context;
  }

  void init()
  {
    // TODO Auto-generated method stub

    this.setContentView(R.layout.nfs);
    this.setTitle(R.string.nfs_tip);

    WindowManager.LayoutParams lp = getWindow().getAttributes();

    lp.width = 600;
    lp.height = 220;

    getWindow().setAttributes(lp);

    mOk = (Button) this.findViewById(R.id.ok);
    mOk.setOnClickListener(this);

    nfsUrl = (EditText) findViewById(R.id.input);
  }

  @Override
  public void onClick(View v)
  {
    if (v == mOk)
    {
      this.dismiss();
      playNfsVideo(nfsUrl.getText().toString());
    }
  }

  private void playNfsVideo(String str)
  {
    Log.e(TAG, "str = " + str);
    String mountPoint = "/data/tom/";
    nfsUnmount(mountPoint); // first try to umount the point
    String[] attrs = str.split("\\" + NFS_SPLIT);
    if (attrs.length < 3)
    {
      Toast.makeText(context, "Wrong nfs address format", Toast.LENGTH_SHORT)
          .show();
      return;
    }
    String folder = "/" + attrs[1];
    String file = attrs[2];
    System.out.println("folder = " + folder + " file = " + file + " ip = "
        + attrs[0]);
    boolean success = nfsMount(folder, mountPoint, attrs[0]);
    System.out.println("success = " + success);
    if (success)
    {
      String filename = mountPoint + file;
      File videoFile = new File(filename);
      if (videoFile.exists())
        ((PresentationActivity) context).play(filename,false);
      else
        Toast.makeText(context, "can not find file", Toast.LENGTH_SHORT).show();
    }
    else
      Toast.makeText(context, "can not mount nfs server", Toast.LENGTH_SHORT)
          .show();
  }

  /**
   * Mount the server's all shared folder to local disk busybox mount -o nolock
   * -t nfs 192.168.99.112:/home/wanran/share /sdcard/share There will use jni
   * call system function mount.
   * 
   * @param source
   *          : source(remote) dir, example: /home/wanran/share
   * @param target
   *          : target(local) dir, example: /sdcard/share
   * @param sourceIp
   *          : source(remote) ip addr, example: 192.168.99.112
   * @return success: true fail: false
   */
  public boolean nfsMount(String source, String target, String sourceIp)
  {
    source = sourceIp + ":" + source;
    String opts = NFS_OPTS + sourceIp;
    int ret = 0;
    // First try read and write permission
    ret = SystemMix.mount(source, target, NFS_TYPE, NFS_FLAGS, opts);
    if (ret == 0)
      return true;
    Log.e("NfsDialog", "Mount error, errno=" + ret);
    return false;
  }

  /**
   * Unmount the server's all shared folder from local disk busybox umount
   * /sdcard/share There will use jni call system function unmount
   * 
   * @param target
   *          : target(local) dir, example: /sdcard/share
   * @return success: true fail: false
   */
  public boolean nfsUnmount(String target)
  {
    SystemMix.umount(target);
    return true;
  }

}
