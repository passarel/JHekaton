package br.ufrgs.ppgc.gia.jhekaton;

//XMI State Types Constants
public enum StateType {

	INITIAL("uml:Pseudostate"), FINAL("uml:FinalState"), COMMON("uml:State");
	
	private String umlType;

	private StateType(String umlType) {
		this.umlType = umlType;
	}

	public String getUmlType() {
		return umlType;
	}

}
