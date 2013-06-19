package br.ufrgs.ppgc.gia.jhekaton;

import java.util.List;

import br.ufrgs.ppgc.gia.jhekaton.xml.Model;

public class Summary {

	private Boolean fullLearning;
	
	private int states;
	private int transitions;
	private int epochs;
	private double trainningError;
	
	private List<List<Object>> trainningSet;
	
	private int inputs;
	private int hiddens;
	private int outputs;
	
	private Model.PackagedElement.Region newDiagram;

	public int getStates() {
		return states;
	}

	public void setStates(int states) {
		this.states = states;
	}

	public int getTransitions() {
		return transitions;
	}

	public void setTransitions(int transitions) {
		this.transitions = transitions;
	}

	public int getEpochs() {
		return epochs;
	}

	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	public String getTopology() {
		return this.inputs+":"+this.hiddens+":"+this.outputs;
	}

	public int getInputs() {
		return inputs;
	}

	public void setInputs(int inputs) {
		this.inputs = inputs;
	}

	public int getHiddens() {
		return hiddens;
	}

	public void setHiddens(int hiddens) {
		this.hiddens = hiddens;
	}

	public int getOutputs() {
		return outputs;
	}

	public void setOutputs(int outputs) {
		this.outputs = outputs;
	}

	public double getTrainningError() {
		return trainningError;
	}

	public void setTrainningError(double trainningError) {
		this.trainningError = trainningError;
	}

	public List<List<Object>> getTrainningSet() {
		return trainningSet;
	}

	public void setTrainningSet(List<List<Object>> trainningSet) {
		this.trainningSet = trainningSet;
	}

	public Boolean getFullLearning() {
		return fullLearning;
	}

	public void setFullLearning(Boolean fullLearning) {
		this.fullLearning = fullLearning;
	}

	public Model.PackagedElement.Region getNewDiagram() {
		return newDiagram;
	}

	public void setNewDiagram(Model.PackagedElement.Region newDiagram) {
		this.newDiagram = newDiagram;
	}
}