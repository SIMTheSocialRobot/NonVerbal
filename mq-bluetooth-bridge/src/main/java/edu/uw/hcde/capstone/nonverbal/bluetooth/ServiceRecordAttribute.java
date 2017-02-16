package edu.uw.hcde.capstone.nonverbal.bluetooth;

public enum ServiceRecordAttribute {
	
	ServiceRecordHandle(0x0000),
	ServiceClassIDList(0x0001),
	State(0x0002),
	ID(0x0003),
	ProtocolDescriptorList(0x0004),
	BrowseGroupList(0x0005),
	LanguageBasedAttributeIDList(0x006);
	
	private int v;
	private ServiceRecordAttribute(int v) {
		this.v = v;
	}
	
	public int getValue() {
		return v;
	}
}
