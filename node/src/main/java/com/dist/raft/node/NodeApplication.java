package com.dist.raft.node;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.json.JSONObject;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import static java.util.Collections.max;

@SpringBootApplication
public class NodeApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(NodeApplication.class, args);
	}

	public static StateManager state;
	public static TimerTask electionTimeoutTask;
	public static Timer electionTimeout;
	public static TimerTask heartBeatTask;
	public static Timer heartBeat;
	public static String[] nodeList = new String[]{"node1", "node2", "node3", "node4", "node5"};
	private final Long timeout = (long) ((Math.random() * (350 - 250)) + 250);
	private int votes = 0;
	
	@Value("${NODENAME}")
	private String nodeName;

	public Integer getPort(String sender){

		return 8085;
	}

	public InetAddress getIP(String node){
		InetAddress ip = null;
		try{
			ip = InetAddress.getByName(node);
		}catch(Exception ignored){}
		return ip;
	}

	public void sendLeaderInfo(Message msgObj){
		Message newMsg = new Message(state.getNodeName(), "LEADER_INFO", state.getCurrentTerm(),
				"Leader", state.getCurrLeader());
		InetAddress ip = getIP(msgObj.getSender_name());
		if(ip == null){
			System.out.println("Controller not active");
			return;
		}
		Thread sender = new Thread(new Sender(newMsg, ip, getPort(msgObj.getSender_name())));
		sender.start();
	}

	public void processMessage(Message msgObj) {
		//Check the type of message sender
		if(msgObj.getSender_name().equalsIgnoreCase("controller")) {
			processControllerMessage(msgObj);
		}
		else {
			if(state.getStatus().equalsIgnoreCase("INACTIVE")){
				System.out.println("............NODE IS INACTIVE............");
				return;
			}
			processRaftMessage(msgObj);
		}
	}

	public void processControllerMessage(Message msgObj) {
		if(!msgObj.getRequest().equalsIgnoreCase("CONVERT_FOLLOWER")){
			if(state.getStatus().equalsIgnoreCase("INACTIVE")){
				System.out.println("............NODE IS INACTIVE............");
				return;
			}
		}
		switch (msgObj.getRequest()) {
			case "CONVERT_FOLLOWER" -> {
				if (state.getStatus().equalsIgnoreCase("LEADER")) {
					toggleHeartBeat(false);
				}
				state.setStatus("FOLLOWER");
				toggleElectionTimeout(true);
			}
			case "TIMEOUT" -> {
				if (!state.getStatus().equalsIgnoreCase("LEADER")) {
					if(electionTimeout != null){
						electionTimeout.cancel();
						electionTimeout.purge();
					}
					electionTimeoutTask.run();
				}
			}
			case "SHUTDOWN" -> {
				state.setStatus("INACTIVE");
				toggleElectionTimeout(false);
				toggleHeartBeat(false);
			}
			case "LEADER_INFO" -> {
				sendLeaderInfo(msgObj);
			}
			case "STORE" -> {
				if(!state.getStatus().equalsIgnoreCase("LEADER")){
					sendLeaderInfo(msgObj);
				}
				else{
					
					processLogs(msgObj.getKey(), msgObj.getValue());
					
				}
			}
			case "RETRIEVE" -> {
				if(!state.getStatus().equalsIgnoreCase("LEADER")){
					sendLeaderInfo(msgObj);
				}
				else{
//					List<JSONObject> committedLogs = state.getLog().subList(0, state.getCommitIndex()+1);
					//List<JSONObject> committedLogs = state.getLog();
					Message newMsg = new Message(state.getNodeName(), "RETRIEVE", state.getCurrentTerm(),
							"COMMITED_LOGS", state.getLogs().toString());
					//newMsg.getEntries().add(state.getLog());
					InetAddress ip = getIP(msgObj.getSender_name());
					if(ip == null){
						System.out.println("Controller not active");
						return;
					}
					Thread sender = new Thread(new Sender(newMsg, ip, getPort(msgObj.getSender_name())));
					System.out.println("Reply sent to controller");
					sender.start();
				}
			}
		}
	}

	public void processRaftMessage(Message msgObj) {
		switch (msgObj.getRequest()) {
			case "APPEND_RPC" -> {
				System.out.println("Received heartbeat from " + msgObj.getSender_name()+" for term "
						+msgObj.getTerm());
				
				System.out.println("Logs " + state.getLogs());

				//FOLLOWER - Receives heartbeat and sets term and leader
				if (state.getStatus().equalsIgnoreCase("FOLLOWER")) {
					toggleElectionTimeout(true);
					state.setCurrLeader(msgObj.getSender_name());
					state.setCurrentTerm(msgObj.getTerm());
					state.setVotedFor("");
					toggleElectionTimeout(true);
					
		
					
					try {
						Message msg = new Message();
						if (msgObj.getEntry()!= null) {
							HashMap<String, String> entry = msgObj.getEntry();
							if (msgObj.getPrevLogIndex() == state.getLogs().size() ) {
								
								state.logs.add(entry);
								msg.setSuccess(true);
							}
							//
							msg.setCommitIndex(state.getLogs().size());
						} else {
							msg.setSuccess(false);
						}
						msg.setRequest("APPEND_REPLY");
						msg.setSender_name(state.getNodeName());
						InetAddress ip = getIP(msgObj.getSender_name());
						if(ip == null){
							System.out.println("Leader is dead");
							break;
						}
						
						//System.out.println(state.getLogs().toString());
						Thread sender = new Thread(new Sender(msg, ip, getPort(msgObj.getSender_name())));
						sender.start();
						
						
					}catch(Exception ex) {
						System.out.println(ex.getMessage());
					}
				}

				//CANDIDATE or LEADER - Checks term and decides to become follower if term is higher
				if ((state.getStatus().equalsIgnoreCase("CANDIDATE") ||
						state.getStatus().equalsIgnoreCase("LEADER")) &&
								msgObj.getTerm() >= state.getCurrentTerm()) {
					state.setStatus("FOLLOWER");
					state.setCurrLeader(msgObj.getSender_name());
					state.setCurrentTerm(msgObj.getTerm());
					state.setVotedFor("");
					if (heartBeat != null) {
						heartBeat.cancel();
						heartBeat.purge();
					}
					toggleElectionTimeout(true);
				}
			}
			case "VOTE_REQUEST" -> {
				System.out.println("Vote request received from "+msgObj.getSender_name()+" for term "
				+msgObj.getTerm());

				//LEADER - Ignores vote requests
				if (state.getStatus().equalsIgnoreCase("LEADER")) {
					break;
				}

				//FOLLOWER and CANDIDATE - Check if already voted for the term and vote if not voted

				if ((msgObj.getTerm() > state.getCurrentTerm()) || (msgObj.getTerm() == state.getCurrentTerm() && msgObj.getLastLogIndex() > state.getLogs().size())) {
					state.setCurrentTerm(msgObj.getTerm());
					state.setVotedFor(msgObj.getSender_name());
					state.setStatus("FOLLOWER");
					Message newMsg = new Message(state.getNodeName(), "VOTE_ACK", state.getCurrentTerm());
					InetAddress ip = getIP(msgObj.getSender_name());
					if(ip == null){
						System.out.println("CANDIDATE is dead");
						break;
					}
					Thread sender = new Thread(new Sender(newMsg, ip, getPort(msgObj.getSender_name())));
					sender.start();
					System.out.println("Voted " + msgObj.getSender_name()+" for term "+msgObj.getTerm());
					toggleElectionTimeout(true);
				}
			}
			case "VOTE_ACK" -> {
				//If not CANDIDATE, drop the packet
				if(!state.getStatus().equalsIgnoreCase("CANDIDATE")){
					System.out.println("I got a vote but not a candidate sadly");
					break;}

				//Increase vote count and set self to leader if count reaches majority
				System.out.println("Got a vote from "+msgObj.getSender_name()+" for term "+msgObj.getTerm());
				votes += 1;
                //As the number of nodes are 5, the majority is achieved when we receive at least 2 votes from
                //other nodes. (2 votes from other nodes + self vote = 3 votes)
                //As 3 > 5/2, we have achieved majority votes
				if (votes >= 2) {
					state.setStatus("LEADER");
					System.out.println(state.getNodeName()+" is the leader for term " + state.getCurrentTerm());
					state.setCurrLeader(state.getNodeName());
					
					
					for (int i = 0; i < state.nextIndex.length; i++) {
						state.nextIndex[i] = state.getLogs().size() + 1;
						state.matchIndex[i] = state.getLogs().size();
					}

					//Start heartbeat and stop election timeout
					toggleElectionTimeout(false);
					toggleHeartBeat(true);
				}
			}
			
			case "APPEND_REPLY" ->{
				if (msgObj.getCommitIndex() != -1) {
					char nodeIndex = msgObj.getSender_name().charAt(4);
					int index = Integer.parseInt(String.valueOf(nodeIndex));
					index--;
					//System.out.println("Commit Index in Append_reply "+ index + " and " + msgObj.getCommitIndex());
					state.matchIndex[index] = msgObj.getCommitIndex();
					state.nextIndex[index] = msgObj.getCommitIndex() +1 ;
					//System.out.println("The achieved match index is "+ state.matchIndex[index] + " and next index is " +state.nextIndex[index] );
				}
				
			}
		}
		
	}

	public void toggleElectionTimeout(Boolean set){
		if(set){
			electionTimeoutTask = new TimerTask() {
				@Override
				public void run() {
					if(!state.getStatus().equalsIgnoreCase("LEADER")) {
						//Increase own term and change state to CANDIDATE
						state.setCurrentTerm(state.getCurrentTerm() + 1);
						state.setStatus("CANDIDATE");
						System.out.println(state.getNodeName() + " is now a " + state.getStatus() + " for term "
								+ state.getCurrentTerm());

						//Reset vote count
						votes = 0;

						//Votes itself for current term
						state.setVotedFor(state.getNodeName());

						//Requesting votes from all followers
						
						
						// [{term=1, value=v1, key=k1}, {term=1, value=v2, key=k2}]
						
						int lastTerm  = 0;
						int logSize = state.getLogs().size();
						if(logSize!=0) {
							
							//System.out.println("The value is " +state.getLogs().get(logSize-1));
							
							String termKey = state.getLogs().get(logSize -1).toString();
							//System.out.println("Trying to retrieve the value of last " + termKey);
							lastTerm = Integer.parseInt(String.valueOf(termKey.charAt(6)));
							//System.out.println("Trying to retrieve the value of last " + lastTerm);
						}
						
						Message reqVoteRPC = new Message(state.getNodeName(), "VOTE_REQUEST", state.getCurrentTerm(),
								"", "", state.getLogs().size(), lastTerm);
						for (String node : nodeList) {
							if (node.equalsIgnoreCase(state.getNodeName()))
								continue;
							InetAddress ip = getIP(node);
							if (ip == null) {
								System.out.println("Node " + node + " is not active");
								continue;
							}
							System.out.println("Vote request sent to " + node);
							Thread requestThread = new Thread(new Sender(reqVoteRPC, ip, getPort(node)));
							requestThread.start();
						}
						if(state.getStatus().equalsIgnoreCase("CANDIDATE"))
							toggleElectionTimeout(true);
					}
				}
			};
			if(electionTimeout != null){
				electionTimeout.cancel();
				electionTimeout.purge();
			}
			electionTimeout = new Timer("ElectionTimeout");
			electionTimeout.schedule(electionTimeoutTask, state.getTimeoutInterval());
		}
		else{
			if(electionTimeout != null){
				electionTimeout.cancel();
				electionTimeout.purge();
			}
		}
	}

	public void toggleHeartBeat(Boolean set){
		if(set){
			heartBeatTask = new TimerTask() {
				@Override
				public void run() {
						Message heartBeat = new Message(state.getNodeName(), "APPEND_RPC", state.getCurrentTerm(),
								"", "", List.of(), -1, -1);
						for (String node : nodeList) {
							if (node.equalsIgnoreCase(state.getNodeName()))
								continue;
							int index = Integer.parseInt(String.valueOf(node.charAt(4)));
							// Index: 1 to 5 
							index--;
						
							
							if (state.logs.size() >= state.nextIndex[index]) {
							
								HashMap<String, String> entry = state.logs.get(state.matchIndex[index]);

								heartBeat.setPrevLogIndex(state.matchIndex[index]);
								if (state.logs.size() == 0)
									heartBeat.setPrevLogTerm(0);
								else
									heartBeat.setPrevLogTerm(Integer.parseInt(state.logs.get(state.logs.size() - 1).get("term")));
								heartBeat.setEntry(entry);
							
							}
							InetAddress ip = getIP(node);
							if(ip == null){
								System.out.println("Node "+node+" is not active");
								continue;
							}
							Thread heartBeatThread = new Thread(new Sender(heartBeat, ip, getPort(node)));
							heartBeatThread.start();
							
							System.out.println("Sent heartbeat to "+node+" for term "+state.getCurrentTerm());
							System.out.println("Logs" + state.getLogs());
						}
				}
			};
			if(heartBeat != null){
				heartBeat.cancel();
				heartBeat.purge();
			}
			heartBeat = new Timer("Heartbeat");
			heartBeat.schedule(heartBeatTask, state.getHeartbeatInterval(), state.getHeartbeatInterval());
		}
		else{
			if(heartBeat != null){
				heartBeat.cancel();
				heartBeat.purge();
			}
		}
	}

	public void processLogs(String key, String value) {
		if (state.getStatus() == "LEADER") {
			HashMap<String, String> map = new HashMap<>();
			map.put("key", key);
			map.put("value", value);
			map.put("term", state.getCurrentTerm() + "");
			state.getLogs().add(map);
		}

	}

	@Override
	public void run(String... args) throws Exception {

		//Setting the initial state
		state = new StateManager(nodeName, "INACTIVE",
				0, timeout, 100L);

		//Change state to follower
		state.setStatus("FOLLOWER");
		toggleElectionTimeout(true);

		System.out.println("I am "+state.getNodeName()+" in "+state.getStatus()+" state");

		Thread rcv = new Thread(new Receiver(state.getNodeName(), getPort(state.getNodeName())));
		rcv.start();

	}
}
	
