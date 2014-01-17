package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * Gets all locations of a given list of users (iterative). If all locations are fetched, this step sends
 * them.
 * 
 * @author Nico
 * 
 */
// TODO: do parallel for faster processing
public class GetAllLocationsStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetAllLocationsStep.class);

	private final Set<String> userIds;

	public GetAllLocationsStep(Set<String> userIds) {
		this.userIds = userIds;
	}

	@Override
	public void start() {
		Map<String, List<PeerAddress>> allLocations = new HashMap<String, List<PeerAddress>>();

		// iterate over all users and get the locations of them
		for (String userId : userIds) {
			NetworkContent content = get(userId, H2HConstants.USER_LOCATIONS);
			if (content == null) {
				allLocations.put(userId, new ArrayList<PeerAddress>());
			} else {
				Locations currentLoc = (Locations) content;
				List<PeerAddress> addresses = new ArrayList<PeerAddress>(currentLoc.getPeerAddresses());
				allLocations.put(userId, addresses);
			}
		}

		// done with all locations
		logger.debug("Sending notifications to " + allLocations.size() + " users");
		getProcess().setNextStep(getSendingProcessSteps(allLocations));
	}

	private ProcessStep getSendingProcessSteps(Map<String, List<PeerAddress>> allLocations) {
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
		Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();
		BaseNotificationMessageFactory messageFactory = context.getMessageFactory();

		ProcessStep tail = null;
		for (String user : allLocations.keySet()) {
			boolean toOwnUser = false;
			try {
				toOwnUser = user.equals(getNetworkManager().getSession().getCredentials().getUserId());
			} catch (NoSessionException e) {
				// ignore
			}

			List<PeerAddress> peerList = allLocations.get(user);
			logger.debug("Notifying " + peerList.size() + " clients of user '" + user + "'.");
			for (PeerAddress peerAddress : peerList) {
				if (peerAddress.equals(getNetworkManager().getConnection().getPeer().getPeerAddress())) {
					// don't send to own peer
					continue;
				}

				BaseDirectMessage msg = messageFactory.createPrivateNotificationMessage(peerAddress);
				tail = new SendNotificationMessageStep(msg, userPublicKeys.get(user), tail, toOwnUser);
			}
		}

		return tail;
	}
}
