package org.hive2hive.core.process.upload;

import java.security.PublicKey;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;

public class UploadNotificationMessageFactory extends BaseNotificationMessageFactory {

	private final PublicKey fileKey;

	/**
	 * @param fileKey the file that has been added
	 */
	public UploadNotificationMessageFactory(PublicKey fileKey, Set<String> users) {
		super(users);
		this.fileKey = fileKey;
	}

	@Override
	public BaseDirectMessage createPrivateNotificationMessage(PeerAddress receiver) {
		return new UploadNotificationMessage(receiver, fileKey);
	}

	@Override
	public UserProfileTask createUserProfileTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
