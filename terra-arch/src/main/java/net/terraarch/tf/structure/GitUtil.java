package net.terraarch.tf.structure;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class GitUtil {


//	private static final TransportConfigCallback tcc =new TransportConfigCallback() {
//		  @Override
//		  public void configure( Transport transport ) {
//			if (transport instanceof SshTransport) {
//				
//				SshTransport sshTransport = ( SshTransport )transport;
//			//	sshTransport.setSshSessionFactory( sshSessionFactory );
//									
//			}		    
//		  }
//	};

//	//https://www.codeaffine.com/2014/12/09/jgit-authentication/
//	private static final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
//		 //JSchConfigSessionFactory is mostly compatible with OpenSSH, 
//		 //the SSH implementation used by native Git. It loads the known 
//		 //hosts and private keys from their default locations 
//		 //(identity, id_rsa and id_dsa) in the user’s .ssh directory.
//		  @Override
//		  protected void configure( Host host, Session session ) {
//		    // do nothing
//		  }
//	};

	public static Git clone(String url, String targetFolder) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File f = new File(targetFolder);
		if (!f.exists()) {
			f.mkdirs();
			if (!f.exists() || !f.isDirectory()) {
				throw new UnsupportedOperationException("bad target folder: "+targetFolder);
			}
		}
		///////////delete lock if left behind from previous run
		File lock = new File (targetFolder+"/.git/index.lock");
		if (lock.exists()) {
			lock.delete();
		}
		//////////////////////////////////////////////////////
		
		if (f.listFiles().length>0) {
			Git existing = Git.open(f);
			
			
			//this does not alwasy work, may need to be in try block
			try {
				existing.pull().call(); //simple pull update
			} catch (Throwable t) {
				//ignore
			}
			return existing;
		}
		
		if (url.startsWith("git::")) { //we allow this but not needed for the actual usage.
			url = url.substring(5);
		}
		
		CloneCommand cloneCmd = Git.cloneRepository()
				.setCloneSubmodules(false) ///is this needed?
				//.setProgressMonitor(pm)
				//.setTransportConfigCallback(tcc)
				.setURI(url)  // https://github.com/github/testrepo.git
				.setDirectory(f);			
		
		return cloneCmd.call();
				
	}

	public static String buildFolderPath(String uri) {
			if (uri.startsWith("git::")) {
				uri = uri.substring(5);
			}
			if (uri.startsWith("https://")) {
				uri = uri.substring(8);
			}
			if (uri.startsWith("ssh://")) {
				uri = uri.substring(6);
			}
			if (uri.startsWith("git@")) {
				uri = uri.substring(4);
			}
	//		if (!uri.startsWith("github")) {		
	//			System.out.println(uri);
	//		}
			String folder = uri.replace(".com:",".com"+File.separatorChar)
					           .replace(".","-");
	
			//if we are using windows makes the swap.
			return folder.replace('/', File.separatorChar);
		}
	
	
}
