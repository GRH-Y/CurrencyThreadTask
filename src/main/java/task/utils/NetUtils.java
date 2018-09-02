package task.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by No.9 on 7/11/2017.
 */

public class NetUtils {
    /**
     * 获取本机地址
     *
     * @param netType 网卡类型 比如 lo，wlan0, net0 eth0
     * @return
     * @throws SocketException
     */
    public static String getLocalIp(String netType) {

        String wlan = null;
        String eth = null;
        String ret = null;

        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements() && ret == null) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                String name = netInterface.getName();
                String displayName = netInterface.getDisplayName();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements() && ret == null) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !displayName.contains("VMware") && !displayName.contains("Virtual")) {
                        if (name.contains("wlan")) {
                            wlan = address.getHostAddress();
                        } else if (!name.contains("lo"))//|| name.contains("eth") || name.contains("en")
                        {
                            eth = address.getHostAddress();
                        }
                        if (name.equals(netType)) {
                            ret = address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return ret == null ? wlan == null ? eth == null ? "127.0.0.1" : eth : wlan : ret;
    }
}
