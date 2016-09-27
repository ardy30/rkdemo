package com.android.presentation.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.*;
import android.view.*;
import android.widget.*;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.android.presentation.app.PresentationActivity;
import com.android.presentation.app.R;
import com.softwinner.SystemMix;

public class NfsDialog extends Dialog implements OnClickListener {
    private final String TAG = "NfsDialog.java";
    Context context;
    Button mOk, mNo;
    EditText etNfsServerIp, etServerFolder, etLocalMountPoint;

    private static int NFS_FLAGS = 32768; // NFS mount flags
    private static String NFS_OPTS = "nolock,addr="; // NFS mount options
    private static String NFS_TYPE = "nfs"; // NFS mount type
    public final static String NFS_SPLIT = "/";

    public NfsDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public NfsDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    void init() {
        this.setContentView(R.layout.nfs);
        this.setTitle("请输入下面的信息！挂载请全部输入，卸载只需要输入之前挂载在本地的目录！");

//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//
//        lp.width = 600;
//        lp.height = 220;
//
//        getWindow().setAttributes(lp);

        mOk = (Button) this.findViewById(R.id.ok);
        mOk.setOnClickListener(this);
        mNo = (Button) findViewById(R.id.no);
        mNo.setOnClickListener(this);

        etNfsServerIp = (EditText) findViewById(R.id.et_server_mount_ip);
        etServerFolder = (EditText) findViewById(R.id.et_server_folder);
        etLocalMountPoint = (EditText) findViewById(R.id.et_local_mount_point);
    }

    @Override
    public void onClick(View v) {
        if (v == mOk) {
            this.dismiss();
            mountNFS(etNfsServerIp.getText().toString().trim(),
                    etServerFolder.getText().toString().trim(), etLocalMountPoint.getText().toString().trim());
            Toast.makeText(context, "Wrong nfs address format", Toast.LENGTH_SHORT)
                    .show();
        } else if (v == mNo) {
            if (nfsUnmount(etLocalMountPoint.getText().toString().trim())) {
                Toast.makeText(context, "卸载成功！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mountNFS(String serverIp, String serverFolder, String localMountPoint) {
        nfsUnmount(localMountPoint); // first try to umount the point
        boolean success = nfsMount(serverFolder, localMountPoint, serverIp);
        if (success) {
            Toast.makeText(context, "Nfs server 已经成功挂载到" + localMountPoint, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Nfs server 挂载失败！", Toast.LENGTH_SHORT)
                    .show();
        }
        File nfsMountPointDir = new File(localMountPoint);
        List<File> files = new ArrayList<>();
        File[] fileArray = nfsMountPointDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".mpg");
            }
        });
        if (nfsMountPointDir.exists() && fileArray.length > 0) {
            for (File f :
                    fileArray) {
                files.add(f);
            }
        }
        ((PresentationActivity) getOwnerActivity()).updateFile(files);
    }

    /**
     * Mount the server's all shared folder to local disk busybox mount -o nolock
     * -t nfs 192.168.99.112:/home/wanran/share /sdcard/share There will use jni
     * call system function mount.
     *
     * @param source   : source(remote) dir, example: /home/wanran/share
     * @param target   : target(local) dir, example: /sdcard/share
     * @param sourceIp : source(remote) ip addr, example: 192.168.99.112
     * @return success: true fail: false
     */
    public boolean nfsMount(String source, String target, String sourceIp) {
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
     * @param target : target(local) dir, example: /sdcard/share
     * @return success: true fail: false
     */
    public boolean nfsUnmount(String target) {
        int result = SystemMix.umount(target);
        ((PresentationActivity) getOwnerActivity()).updateFile(new ArrayList<File>());
        if (result == 0)
            return true;
        else {
            Toast.makeText(context, "卸载失败！result = " + result, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
