package org.hive2hive.core.test.processes.util;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.network.messages.AcceptanceReply;

/**
 * Denies all messages; can be useful for some tests
 * 
 * @author Nico, Seppi
 * 
 */
public class DenyingMessageReplyHandler implements ObjectDataReply {
	@Override
	public Object reply(PeerAddress sender, Object request) throws Exception {
		return AcceptanceReply.FAILURE;
	}
}