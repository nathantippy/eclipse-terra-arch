package net.terraarch.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

public class NetworkUtil {
	
	private static final ILog logger = Platform.getLog(NetworkUtil.class);
	
	public static byte[] getAllMacs() {
		try {
			ArrayList<NetworkInterface> macs = new ArrayList<NetworkInterface>();			
			int bytesCount = 0;
			{
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface net = interfaces.nextElement();
					
	
					
					final byte[] localMac = net.getHardwareAddress();
					if (null!=localMac) {
						//logger.error("ALL.TEST.MAC:" + net+  " "+net.getDisplayName()+"  "+net.getName()+"  "+Arrays.toString(net.getHardwareAddress()));
	
						final String lowerName = net.getName().toLowerCase()+" "+net.getDisplayName().toLowerCase();
						if (
							   (!lowerName.contains("docker"))
							&& (!lowerName.contains("bluetooth"))
							&& (!lowerName.contains("virtual"))
							&& (!lowerName.startsWith("br-"))
							&& (!lowerName.startsWith("wl"))					
							&& (!lowerName.startsWith("wi-fi"))	
							&& (!lowerName.startsWith("wireless"))	
								) {
						    macs.add(net);
						    bytesCount += localMac.length;			    
					    }
					}
				}
			}
			if (macs.isEmpty()) { //only done if we filtered everything out above.
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface net = interfaces.nextElement();
					final byte[] localMac = net.getHardwareAddress();
					if (null!=localMac) {
						final String lowerName = net.getName().toLowerCase()+" "+net.getDisplayName().toLowerCase();
						if ( 
							   (!lowerName.contains("docker"))
							&& (!lowerName.contains("virtual"))
								) {
						    macs.add(net);
						    bytesCount += localMac.length;			    
					    }
					}
				}	
			}
			
			Comparator<NetworkInterface> comp = new Comparator<NetworkInterface>() {
				@Override
				public int compare(NetworkInterface a, NetworkInterface b) {
					try {
						byte[] aBytes = a.getHardwareAddress();
						byte[] bBytes = b.getHardwareAddress();
						long aLong = toLong(aBytes);
						long bLong = toLong(bBytes);
						return Long.compare(aLong,  bLong);
					} catch (SocketException e) {
						e.printStackTrace();
						return 0;
					}
				}
	
				private long toLong(byte[] aBytes) {
					long result = 0;
					for(int i = 0; i<aBytes.length; i++) {
						result = (result<<8)| (0xFF&(int)aBytes[i]);
					}
					return result;
				}
			};
			Collections.sort(macs,comp );
			byte[] allMac = new byte[bytesCount];
			int allMacPos = 0;
			for(NetworkInterface ni:macs) {
				//logger.error("TEST.MAC:" + ni+  " "+ni.getDisplayName()+"  "+ni.getName()+"  "+Arrays.toString(ni.getHardwareAddress()));
			    byte[] local = ni.getHardwareAddress();
				System.arraycopy(local, 0, allMac, allMacPos, local.length);
				allMacPos += local.length;	        
			}
			//logger.error("final value: "+UUID.nameUUIDFromBytes(allMac).toString());
			return allMac;
		} catch (Throwable t) {
			logger.error("getAllMacs",t);
		 	return "unknown".getBytes();
		}
	}

}
