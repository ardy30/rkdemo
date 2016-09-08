package com.android.presentation.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.android.presentation.app.util.ToastUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @auth 俊
 * Created by 俊 on 2016/8/29.
 */
public class SettingActivity extends Activity implements View.OnClickListener {
    /**
     * 有线网开关
     */
    private Switch switchEthernet;
    /**
     * 连接方式选择组
     */
    private RadioGroup rgConnectWay;
    private LinearLayout llStaticEthernet;
    private EditText etIp, etMask, etGateway, etDns;
    private Button btnConnect;
    /**
     * 有线网管理对象
     */
    private EthernetManager mEthManager;
    /**
     * 手动设置ip时需要的配置对象
     */
    private IpConfiguration mIpConfiguration;
    /**
     * 是否以DHCP方式连接有线网
     */
    private boolean isDhcp = true;
    /**
     * 是否是手动在点击有线网开关
     */
    private boolean isFromUser;
    private BroadcastReceiver mReceiver;
    private String mEthIp, mEthMask, mEthGateway, mEthDns;

    /**
     * 初始化广播接收器来接收有线网连接状态
     */
    private void initReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(EthernetManager.ETHERNET_STATE_CHANGED_ACTION)) {
                    int state = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE, -1);
                    handleState(state);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mEthManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
        initReceiver();
        initViews();
        getEthInfo();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        //有线开关
        switchEthernet = (Switch) findViewById(R.id.switch_ethernet);
        switchEthernet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isFromUser = true;
                ToastUtil.showToastAndCancel(SettingActivity.this,
                        "点击了Switch开关按钮", true);
                // 在此只是为了确定是否为用户在点击操作，返回false来告诉系统没有在此没有做处理，
                // 这样系统才能执行onCheckedChanged方法
                return false;
            }
        });
        switchEthernet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isFromUser) {
                    if (b) {
                        //开启有线网
                        if (isDhcp) {
                            boolean result = mEthManager.setEthernetEnabled(true);
                            ToastUtil.showToastAndCancel(SettingActivity.this,
                                    "有线网开启结果：" + result + "\n 接下来请点击连接", true);
                        } else {
                            boolean result = mEthManager.setEthernetEnabled(true);
                            ToastUtil.showToastAndCancel(SettingActivity.this,
                                    "有线网开启结果：" + result + "\n 接下来请先配置下面静态ip信息再点击连接", true);
                        }
                        btnConnect.setEnabled(true);
                        btnConnect.setText("连接");
                    } else {
                        //关闭有线网
                        mEthManager.setEthernetEnabled(false);
                        btnConnect.setEnabled(false);
                        btnConnect.setText("请先开启开关");
                    }
                }
                isFromUser = false;
            }
        });
        //手动设置ip和显示信息的布局
        llStaticEthernet = (LinearLayout) findViewById(R.id.ll_ethernet);
        if (isDhcp)
            llStaticEthernet.setVisibility(View.GONE);
        //RadioGruop
        rgConnectWay = (RadioGroup) findViewById(R.id.rg_connect_way);
        rgConnectWay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_dhcp:
                        llStaticEthernet.setVisibility(View.GONE);
                        isDhcp = true;
                        break;
                    case R.id.rb_static:
                        llStaticEthernet.setVisibility(View.VISIBLE);
                        isDhcp = false;
                        break;
                }
            }
        });
        //输入框
        etIp = (EditText) findViewById(R.id.et_ip);
        etMask = (EditText) findViewById(R.id.et_mask);
        etGateway = (EditText) findViewById(R.id.et_gateway);
        etDns = (EditText) findViewById(R.id.et_dns);
        //连接按钮
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == btnConnect) {
            //连接有线网
            connectEthernet(isDhcp);
        }
    }

    /**
     * 连接有线网
     *
     * @param isDhcp
     */
    private void connectEthernet(boolean isDhcp) {
        if (isDhcp) {
            connectByDhcp();
        } else {
            boolean result = connectByStatic();
            if (!result) {
                ToastUtil.showToastAndCancel(this, "连接失败", true);
            }
        }
    }

    /**
     * 自动获取IP方式连接
     */
    private void connectByDhcp() {
        try {
            mEthManager.setConfiguration(new IpConfiguration(
                    IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, null));
        } catch (Exception e) {
            ToastUtil.showToastAndCancel(this, "你没有系统权限，请用系统签名来获取权限！", true);
        }
    }

    /**
     * 手动设置静态IP方式连接
     */
    private boolean connectByStatic() {
        mEthIp = etIp.getText().toString().trim();
        mEthMask = etMask.getText().toString().trim();
        mEthGateway = etGateway.getText().toString().trim();
        mEthDns = etDns.getText().toString().trim();
        StaticIpConfiguration mStaticIpConfiguration = new StaticIpConfiguration();
        Inet4Address inetIp = getIPv4Address(this.mEthIp);
        int prefixLength = transferMaskStrToInetMask(this.mEthMask);
        InetAddress inetGateway = getIPv4Address(this.mEthGateway);
        InetAddress inetDns = getIPv4Address(this.mEthDns);
        //如果ip、网关、dns中有一个对象为null，则表示连接失败
        if (inetIp == null || inetGateway == null || inetDns == null) {
            return false;
        }
        //如果ip、网关、dns对象中对应内容有一个为空字符串或者掩码转换成int类型后的值为0，则同样表示连接失败
        if (inetIp.getAddress().toString().isEmpty() || prefixLength == 0
                || inetGateway.toString().isEmpty()
                || inetDns.toString().isEmpty()) {
            return false;
        }
        mStaticIpConfiguration.ipAddress = new LinkAddress(inetIp,
                prefixLength);
        mStaticIpConfiguration.gateway = inetGateway;
        mStaticIpConfiguration.dnsServers.add(inetDns);
        mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC,
                IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
        try {
            mEthManager.setConfiguration(mIpConfiguration);
        } catch (Exception e) {
            ToastUtil.showToastAndCancel(this, "你没有系统权限，请用系统签名来获取权限！", true);
        }
        return true;
    }

    /**
     * 将输入的IP地址字符串转换为Inet4Address对象
     *
     * @param text
     * @return
     */

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            ToastUtil.showToastAndCancel(this, "输入有误，请检查！");
            return null;
        }
    }

    /**
     * 将掩码转哈un为int类型的前缀长度
     */
    private int transferMaskStrToInetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        Pattern pattern = Pattern
                .compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (pattern.matcher(maskStr).matches() == false) {
            return 0;
        }
        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer
                    .parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }

    /**
     * 处理有线网络连接状态
     *
     * @param state
     */
    private void handleState(int state) {
        switch (state) {
            case EthernetManager.ETHER_STATE_DISCONNECTED:
                mEthIp = "";
                mEthMask = "";
                mEthGateway = "";
                mEthDns = "";
                break;
            case EthernetManager.ETHER_STATE_CONNECTING:
                String mStatusString = "连接获取中...";
                mEthIp = mStatusString;
                mEthMask = mStatusString;
                mEthGateway = mStatusString;
                mEthDns = mStatusString;
                break;
            case EthernetManager.ETHER_STATE_CONNECTED:
                getEthInfo();
                break;
        }
        refreshUI();
    }

    /**
     * 获取当前连接信息
     */
    private void getEthInfo() {
        try {
            if (mEthManager.getEthernetConnectState() == EthernetManager.ETHER_STATE_CONNECTED) {
                switchEthernet.setChecked(true);
                ToastUtil.showToastAndCancel(this, "正在获取并显示当前连接的有线网信息！", true);
                IpConfiguration.IpAssignment mode = mEthManager.getConfiguration().getIpAssignment();
                if (mode == IpConfiguration.IpAssignment.DHCP) {
                    isDhcp = true;
                    rgConnectWay.check(R.id.rb_dhcp);
                    getEthInfoFromDhcp();
                } else if (mode == IpConfiguration.IpAssignment.STATIC) {
                    isDhcp = false;
                    rgConnectWay.check(R.id.rb_static);
                    getEthInfoFromStaticIp();
                }
                llStaticEthernet.setVisibility(View.VISIBLE);
            } else {
                ToastUtil.showToastAndCancel(this, "当前有线网没有连接！", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastAndCancel(this, "你没有系统权限，请用系统签名来获取权限！", true);
        }
    }

    /**
     * 获取DHCP连接的配置信息
     */
    private void getEthInfoFromDhcp() {
        String tempIpInfo;
        String iface = "eth0";
        tempIpInfo = SystemProperties.get("dhcp." + iface + ".ipaddress");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
            mEthIp = tempIpInfo;
        } else {
            mEthIp = "";
        }
        tempIpInfo = SystemProperties.get("dhcp." + iface + ".mask");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
            mEthMask = tempIpInfo;
        } else {
            mEthMask = "";
        }
        tempIpInfo = SystemProperties.get("dhcp." + iface + ".gateway");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
            mEthGateway = tempIpInfo;
        } else {
            mEthGateway = "";
        }
        tempIpInfo = SystemProperties.get("dhcp." + iface + ".dns1");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
            mEthDns = tempIpInfo;
        } else {
            mEthDns = "";
        }
    }

    /**
     * 获取静态IP连接的配置信息
     */
    private void getEthInfoFromStaticIp() {
        StaticIpConfiguration staticIpConfiguration = mEthManager
                .getConfiguration().getStaticIpConfiguration();
        if (staticIpConfiguration == null) {
            return;
        }
        LinkAddress ipAddress = staticIpConfiguration.ipAddress;
        InetAddress gateway = staticIpConfiguration.gateway;
        ArrayList<InetAddress> dnsServers = staticIpConfiguration.dnsServers;
        if (ipAddress != null) {
            mEthIp = ipAddress.getAddress().getHostAddress();
            mEthMask = interMask2String(ipAddress.getPrefixLength());
        }
        if (gateway != null) {
            mEthGateway = gateway.getHostAddress();
        }
        mEthDns = dnsServers.get(0).getHostAddress();
    }

    /**
     * 将子网掩码转换成ip子网掩码形式，比如输入32输出为255.255.255.255
     *
     * @param prefixLength
     */
    private String interMask2String(int prefixLength) {
        String netMask = null;
        int inetMask = prefixLength;
        int part = inetMask / 8;
        int remainder = inetMask % 8;
        int sum = 0;
        for (int i = 8; i > 8 - remainder; i--) {
            sum = sum + (int) Math.pow(2, i - 1);
        }
        if (part == 0) {
            netMask = sum + ".0.0.0";
        } else if (part == 1) {
            netMask = "255." + sum + ".0.0";
        } else if (part == 2) {
            netMask = "255.255." + sum + ".0";
        } else if (part == 3) {
            netMask = "255.255.255." + sum;
        } else if (part == 4) {
            netMask = "255.255.255.255";
        }
        return netMask;
    }

    /**
     * 刷新界面显示
     */

    private void refreshUI() {
        etIp.setText(mEthIp);
        etMask.setText(mEthMask);
        etGateway.setText(mEthGateway);
        etDns.setText(mEthDns);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }
}
