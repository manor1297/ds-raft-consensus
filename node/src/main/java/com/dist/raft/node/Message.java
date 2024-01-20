package com.dist.raft.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Message {
	//"sender_name": null,
	//	  "request": null,
	//	  "term": null,
	//	  "key": null,
	//	  "value": null
	private String sender_name;
	private String request;
	private int term;
	private String key;
	private String value;
	private HashMap<String, String> entry;
	protected ArrayList<HashMap<String, String>> entries;

	//For APPEND_ENTRY
//	private List<String> entries;
	private int prevLogIndex =-1;
	public int getCommitIndex() {
		return commitIndex;
	}

	public void setCommitIndex(int commitIndex) {
		this.commitIndex = commitIndex;
	}

	private int prevLogTerm =-1;
	private int leaderCommit;
	
	private int commitIndex =-1;

	public void setEntry(HashMap<String, String> entry) {
		this.entry = entry;
	}

	//For APPEND_REPLY
	private boolean success;

	//For REQUEST_VOTE
	private int lastLogIndex=-1;
	private int lastLogTerm =-1;

	//Constructor for REQUEST_VOTE
	public Message(String sender_name, String request, int term, String key, String value, int lastLogIndex, int lastLogTerm) {
		this.sender_name = sender_name;
		this.request = request;
		this.term = term;
		this.key = key;
		this.value = value;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}

	//Constructor for APPEND_ENTRY
	public Message(String sender_name, String request, int term, String key, String value, List<String> entries, int prevLogIndex, int prevLogTerm) {
		this.sender_name = sender_name;
		this.request = request;
		this.term = term;
		this.key = key;
		this.value = value;
		//this.entries = entries;
		this.prevLogIndex = prevLogIndex;
		this.prevLogTerm = prevLogTerm;
	}

	//Constructor for VOTE_ACK
	public Message(String sender_name, String request, int term) {
		super();
		this.sender_name = sender_name;
		this.request = request;
		this.term = term;
		this.key = "";
		this.value = "";
	}

	//Constructor for LEADER_INFO
	public Message(String sender_name, String request, int term, String key, String value) {
		this.sender_name = sender_name;
		this.request = request;
		this.term = term;
		this.key = key;
		this.value = value;
	}


	//Constructor for controller
	public Message(String sender_name, String request) {
		this.sender_name = sender_name;
		this.request = request;
	}
	
	public Message() {
	}

	public String getSender_name() {
		return sender_name;
	}

	public void setSender_name(String sender_name) {
		this.sender_name = sender_name;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public int getTerm() {
		return term;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

//	public List<String> getEntries() {
//		return entries;
//	}
//
//	public void setEntries(List<String> entries) {
//		this.entries = entries;
//	}

	public int getPrevLogIndex() {
		return prevLogIndex;
	}

	public ArrayList<HashMap<String, String>> getEntries() {
		return entries;
	}

	public void setEntries(ArrayList<HashMap<String, String>> entries) {
		this.entries = entries;
	}

	public HashMap<String, String> getEntry() {
		return entry;
	}

	public void setPrevLogIndex(int prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}

	public int getPrevLogTerm() {
		return prevLogTerm;
	}

	public void setPrevLogTerm(int prevLogTerm) {
		this.prevLogTerm = prevLogTerm;
	}

	public int getLastLogIndex() {
		return lastLogIndex;
	}

	public void setLastLogIndex(int lastLogIndex) {
		this.lastLogIndex = lastLogIndex;
	}

	public int getLastLogTerm() {
		return lastLogTerm;
	}

	public void setLastLogTerm(int lastLogTerm) {
		this.lastLogTerm = lastLogTerm;
	}

	public int getLeaderCommit() {
		return leaderCommit;
	}

	public void setLeaderCommit(int leaderCommit) {
		this.leaderCommit = leaderCommit;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
