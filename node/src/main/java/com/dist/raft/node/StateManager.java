package com.dist.raft.node;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StateManager {
	private String nodeName;
	private String status;
	private String currLeader;

	//RAFT persistent storage
	private Integer currentTerm;
	private String votedFor;
	//private List<JSONObject> log;
	private Long timeoutInterval;
	private Long heartbeatInterval;

	//Volatile Storage
	private Integer commitIndex = -1;
	private Integer lastApplied;
	
	public ArrayList<HashMap<String, String>> logs = new ArrayList<>();
	public int nextIndex[] = new int[] { 1, 1, 1, 1, 1 , 1};
	public int matchIndex[] = new int[] { 0, 0, 0, 0, 0 , 0};
	
	public ArrayList<HashMap<String, String>> getLogs() {
		return logs;
	}

	public void setLogs(ArrayList<HashMap<String, String>> logs) {
		this.logs = logs;
	}

	
	public void setNextIndex(int[] nextIndex) {
		this.nextIndex = nextIndex;
	}

	public void setMatchIndex(int[] matchIndex) {
		this.matchIndex = matchIndex;
	}

	public StateManager() {
		super();
	}
	
	public StateManager(String nodeName, String status, Integer currentTerm, Long timeoutInterval,
			Long heartbeatInterval) {
		super();
		this.nodeName = nodeName;
		this.status = status;
		this.currentTerm = currentTerm;
		this.timeoutInterval = timeoutInterval;
		this.heartbeatInterval = heartbeatInterval;
		this.currLeader = "";
		this.votedFor = "";
		//this.log = new ArrayList<JSONObject>();
	}

	public String getCurrLeader() {
		return currLeader;
	}

	public void setCurrLeader(String currLeader) {
		this.currLeader = currLeader;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Integer getCurrentTerm() {
		return currentTerm;
	}

	public void setCurrentTerm(Integer currentTerm) {
		this.currentTerm = currentTerm;
	}

	public String getVotedFor() {
		return votedFor;
	}

	public void setVotedFor(String votedFor) {
		this.votedFor = votedFor;
	}

//	public List<JSONObject> getLog() {
//		return log;
//	}
//
//	public void setLog(List<JSONObject> log) {
//		this.log = log;
//	}

	public Long getTimeoutInterval() {
		return timeoutInterval;
	}

	public void setTimeoutInterval(Long timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
	}

	public Long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(Long heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public Integer getCommitIndex() {
		return commitIndex;
	}

	public void setCommitIndex(Integer commitIndex) {
		this.commitIndex = commitIndex;
	}

	public Integer getLastApplied() {
		return lastApplied;
	}

	public void setLastApplied(Integer lastApplied) {
		this.lastApplied = lastApplied;
	}

//	public List<Integer> getNextIndex() {
//		return nextIndex;
//	}
//
//	public void setNextIndex(List<Integer> nextIndex) {
//		this.nextIndex = nextIndex;
//	}
//
//	public List<Integer> getMatchIndex() {
//		return matchIndex;
//	}
//
//	public void setMatchIndex(List<Integer> matchIndex) {
//		this.matchIndex = matchIndex;
//	}
}
