package br.ufrgs.ppgc.gia.jhekaton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Greedy;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.CalculateScore;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.pattern.ElmanPattern;

import br.ufrgs.ppgc.gia.jhekaton.xml.Model;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Subvertex;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Transition;

/**
 * Classe que contém o método de inicialização da aplicação
 * @author Igor Montezano - imsantos@inf.ufrgs.br
 *
 */
public class JHekaton {

	protected static Map<String, Integer> entradas;
	protected static Map<String, Integer> saidas;
	
	private static final int TESTE = 1;
	
	private Region region;
	
	private int numInputs;
	private int numHidden;
	private int numOutputs;
	private List<List<Object>> trainningSet;
	private double[][] arrayEntradas;
	private double[][] arraySaidas;

	public JHekaton(){
		this.entradas = new HashMap<String, Integer>();
		this.saidas = new HashMap<String, Integer>();
	}
	
	public Region parseFile(File file){
		
		JAXBContext jaxbContext;
		// Instancia o Parser
		try {
			jaxbContext = JAXBContext.newInstance(Model.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			//Parseia a entrada
			Model stateMachineDiagram = (Model) jaxbUnmarshaller.unmarshal(file);
			this.region = stateMachineDiagram.getPackagedElement().getRegion();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return this.region;
	}
	
	public Summary extractTopology(){
		Summary result = new Summary();
		
		int countEntradas = 0, countSaidas = 0;
		
		result.setStates(this.region.getSubvertex().size());
		
		// Itera sobre os estados
		for(Subvertex sub : this.region.getSubvertex()){
			// Conta os estados como entradas, descontando o "estado final"
			if(!sub.getType().equalsIgnoreCase(StateType.FINAL.getUmlType())){
				this.entradas.put(sub.getId(), countEntradas);
				System.out.println("Entrada:"+countEntradas+":"+sub.getId()+"-"+sub.getName()); 
				countEntradas++;
			}
			
			// Conta os estados como saída, descontando o "estado inicial"
			if(!sub.getType().equalsIgnoreCase(StateType.INITIAL.getUmlType())){
				this.saidas.put(sub.getId(), countSaidas);
				System.out.println("Saída:"+countSaidas+":"+sub.getId()+"-"+sub.getName());
				countSaidas++;
			}
		}
		
		result.setTransitions(this.region.getTransition().size());
		
		// Itera sobre as transições
		for(Transition t : this.region.getTransition()){
			this.entradas.put(t.getId(), countEntradas);
			System.out.println("Entrada:"+countEntradas+":"+t.getId()+"-"+t.getName());
			countEntradas++;
		}
		
		this.numInputs = countEntradas;
		this.numHidden = this.region.getTransition().size();
		this.numOutputs = countSaidas;
		
		result.setInputs(this.numInputs);
		result.setHiddens(this.numHidden);
		result.setOutputs(this.numOutputs);

		// Generate Learninng Sequences
		this.trainningSet = new ArrayList<List<Object>>();
		Subvertex initialState = XmiUtil.getStatesByType(this.region, StateType.FINAL).get(0);
		JHekaton.geraConjuntoTreinamentoBackwardTransversal(this.trainningSet, this.region, initialState);
		// Remove paths that doesn't finish in the final state
		for(Iterator<List<Object>> p = trainningSet.iterator(); p.hasNext();){
			List<Object> next = p.next();
			Object ultimo = next.get(next.size()-1);
			if(!(ultimo instanceof Subvertex && ((Subvertex)ultimo).getType().equals(StateType.FINAL.getUmlType())) ){
				p.remove();
			}
		}
		result.setTrainningSet(this.trainningSet);
		return result;
	}
	
	public void processa(Summary summary) {
		System.out.println("Network Topology: "+this.numInputs+":"+this.numHidden+":"+this.numOutputs+"\r\n");
		
		System.out.println("\r\n");
		Instancia inst = new Instancia(this.numInputs, this.numOutputs);
		inst.construir(trainningSet.get(0));
		
		// Criação da rede, com 3 camadas			
		ElmanPattern pattern = new ElmanPattern();
		pattern.setActivationFunction(new ActivationSigmoid());
		// Cria rede com camada de entrada dimensionada pela contagem
		pattern.setInputNeurons(this.numInputs);
		// quantidade de camadas ocultas segue o número de transições
		pattern.addHiddenLayer(this.numHidden);
		// ... camada de saída dimensionada pela contagem
		pattern.setOutputNeurons(this.numOutputs);
		BasicNetwork rede = (BasicNetwork)pattern.generate();
		
		// Code block to full transitions learning
		this.generateFullTrainingSet();
		if(summary.getFullLearning()){

			MLDataSet trainingSet = new BasicMLDataSet(this.arrayEntradas, this.arraySaidas) ;
			
			CalculateScore score = new TrainingSetScore(trainingSet);

			final MLTrain trainMain = new Backpropagation(rede, trainingSet, 0.0000000001, 0.001);
			final MLTrain trainAlt = new NeuralSimulatedAnnealing(rede, score, 10, 2, 100);

			final StopTrainingStrategy stop = new StopTrainingStrategy();
			trainMain.addStrategy(new Greedy());
			trainMain.addStrategy(new HybridStrategy(trainAlt));
			trainMain.addStrategy(stop);

			int epoch = 0;
			while (!stop.shouldStop()) {
				// cada iteração é uma época
				trainMain.iteration();
				epoch++;
			}
			trainMain.finishTraining();

			
		}
		

		double[][] entradasTreinamento = inst.getEntradas();
		double[][] saidasTreinamento = inst.getSaidas();
		MLDataSet trainingSet = new BasicMLDataSet(entradasTreinamento,saidasTreinamento) ;
		
		CalculateScore score = new TrainingSetScore(trainingSet);

		final MLTrain trainMain = new Backpropagation(rede, trainingSet, 0.0000000001, 0.001);
		final MLTrain trainAlt = new NeuralSimulatedAnnealing(rede, score, 10, 2, 100);

		final StopTrainingStrategy stop = new StopTrainingStrategy();
		trainMain.addStrategy(new Greedy());
		trainMain.addStrategy(new HybridStrategy(trainAlt));
		trainMain.addStrategy(stop);

		int epoch = 0;
		while (!stop.shouldStop()) {
			// cada iteração é uma época
			trainMain.iteration();
			epoch++;
		}
		trainMain.finishTraining();
		summary.setEpochs(epoch);
		System.out.println("Treinamento finalizado em "+epoch+" épocas");
		System.out.println("Erro Final apos treinamento: "+trainMain.getError()+"\r\n");
		summary.setTrainningError(trainMain.getError());
		
		// teste 
		System.out.println("Caso de teste:"+"\r\n");
		for(double i : entradasTreinamento[TESTE]){
			System.out.println(i+"\t");
		}
		System.out.println("\r\n");
		System.out.println("Resultado esperado:\r\n");
		
		for(double i : saidasTreinamento[TESTE]){
			System.out.println(i+"\t");
		}

		System.out.println("\r\n");
		double[] teste1 = entradasTreinamento[TESTE]; // Instancia 1;
		MLData testeSet = new BasicMLData(teste1) ;
		MLData result = rede.compute(testeSet);
		double maior = -1d;
		int index = -1;
		for(int i = 0 ; i < result.getData().length; i++){
			if(maior < result.getData()[i]){
				maior = result.getData()[i];
				index = i;
			}
		}
		System.out.println("\r\n");
		System.out.println("Final: "+index+"\r\n");
		System.out.println("Resultado real:\r\n");
		for(double i : result.getData()){
			System.out.println(i+"\t");
		}
		System.out.println("\r\n");
		
		summary.setNewDiagram(this.recreate(rede));
	}
	
	private Model.PackagedElement.Region recreate(BasicNetwork rede){
		Model.PackagedElement.Region newRegion = new Region();
		
		List<Subvertex> subvertex = this.region.getSubvertex();
		for (Subvertex vertex : subvertex) {			
			newRegion.getSubvertex().add(vertex);
		}

		String[] inputs = new String[JHekaton.entradas.size()];
		for(Iterator<Map.Entry<String, Integer>> iter = JHekaton.entradas.entrySet().iterator(); iter.hasNext(); ){
			Entry<String, Integer> next = iter.next();
			inputs[next.getValue()] = next.getKey();
		}
		
		String[] outputs = new String[JHekaton.saidas.size()];
		for(Iterator<Map.Entry<String, Integer>> iter = JHekaton.saidas.entrySet().iterator(); iter.hasNext(); ){
			Entry<String, Integer> next = iter.next();
			outputs[next.getValue()] = next.getKey();
		}
		
		for(int i = 0; i < this.arrayEntradas.length; i++){
			
			MLData testeSet = new BasicMLData(this.arrayEntradas[i]);
			MLData result = rede.compute(testeSet);

			String source = inputs[this.inputState(this.arrayEntradas[i])];
			String target = outputs[this.maxIndex(result.getData())];
			
			Transition transitionElected = new Transition();
			transitionElected.setId(source+"_"+target);
			transitionElected.setSource(source);
			transitionElected.setTarget(target);
			 
			newRegion.getTransition().add(transitionElected);
		}
		return newRegion;
	}
	
	private int inputState(double[] data){
		for (int i = 0; i < data.length; i++) {
			if(data[i] < 0){
				return i;
			}
		}
		return -1;
	}
	
	private int maxIndex(double[] data){
		int result = 0;
		double min = Double.MAX_VALUE;
		for (int j = 0; j < data.length; j++) {
			if(data[j] < min){
				min = data[j];
				result = j;
			}
		}
		return result;
	}
	
	private void generateFullTrainingSet(){

		// Definition
		List<double[]> entradas = new ArrayList<double[]>();
		List<double[]> saidas   = new ArrayList<double[]>();
		
		
		for(int i = 0; i < this.region.getTransition().size(); i++ ){
			// Inicialization
			double[] entrada = new double[this.numInputs];
			for (int i1 = 0; i1 < entrada.length; i1++) {
				entrada[i1] = 1;
			}
			double[] saida = new double[this.numOutputs];
			for (int i2 = 0; i2 < saida.length; i2++) {
				saida[i2] = 1;
			}

			Transition t = this.region.getTransition().get(i);
			
			entrada[JHekaton.entradas.get(t.getSource())] = -1;
			entrada[JHekaton.entradas.get(t.getId())] = -1;
			
			saida[JHekaton.saidas.get(t.getTarget())] = -1;
			
			entradas.add(entrada);
			saidas.add(saida);
			
		}
		
		this.arrayEntradas = entradas.toArray(new double[entradas.size()][this.numInputs]);
		this.arraySaidas   = saidas.toArray(new double[saidas.size()][this.numOutputs]);
	}
	
	private static void geraConjuntoTreinamentoBackwardTransversal(List<List<Object>> result, Region diagram, Subvertex state){
		List<Transition> transitionsToState = XmiUtil.getTransitionsToState(diagram, state.getId());
		if(transitionsToState != null) {
			for(Transition e :  transitionsToState){
				Subvertex w = XmiUtil.getStateById(diagram, e.getSource());
				if(w.getType().equals(StateType.INITIAL.getUmlType())){
					List<Object> path = new ArrayList<Object>();
					result.add(path);
				}
				if(!state.getId().equals(w.getId())){
					geraConjuntoTreinamentoBackwardTransversal(result, diagram, w);
				}
				List list = (List)result.get(result.size()-1);
				if(!list.contains(w)){
					list.add(w);
				}
				list.add(e);				
				list.add(state);
			}		
		}
	}
}

class Instancia {
	
	private int countEntradas;
	private int countSaidas;
	
	private List<double[]> entradas;
	private List<double[]> saidas;

	public Instancia(int countEntradas, int countSaidas) {
		super();
		this.countEntradas = countEntradas;
		this.countSaidas = countSaidas;
	}
	
	public double[][] getEntradas(){
		System.out.println("Entradas:");
		double[][] result = new double[this.entradas.size()][this.countEntradas];
		int i = 0;
		for(double[] ent : this.entradas){
			for (int j = 0; j < ent.length; j++) {
				System.out.print(ent[j]+", ");
			}
			System.out.println();
			result[i] = ent;
			i++;
		}
		System.out.println();
		System.out.println();
		return result;
	}

	public double[][] getSaidas(){
		System.out.println("Saídas");
		double[][] result = new double[this.saidas.size()][this.countSaidas];
		int i = 0;
		for(double[] ent : this.saidas){
			for (int j = 0; j < ent.length; j++) {
				System.out.print(ent[j]+", ");
			}
			System.out.println();
			result[i] = ent;
			i++;
		}
		System.out.println();
		System.out.println();
		return result;
	}

	
	public void construir(List<?> caminho){
		// Definition
		this.entradas = new ArrayList<double[]>();
		this.saidas   = new ArrayList<double[]>();
		
		// Inicialization
		double[] entrada = new double[this.countEntradas];
		for (int i = 0; i < entrada.length; i++) {
			entrada[i] = -1;
		}
		double[] saida = new double[this.countSaidas];
		for (int i = 0; i < saida.length; i++) {
			saida[i] = -1;
		}

		Iterator iter = caminho.iterator();		
		Object o = iter.next();
		while(iter.hasNext()){
			
			if(o instanceof Subvertex){
				entrada[JHekaton.entradas.get(((Subvertex)o).getId())] = 1;
			} else {
				entrada[JHekaton.entradas.get(((Transition)o).getId())] = 1;
			}

			o = iter.next();
			if(o instanceof Subvertex){
				entrada[JHekaton.entradas.get(((Subvertex)o).getId())] = 1;
			} else {
				entrada[JHekaton.entradas.get(((Transition)o).getId())] = 1;
			}

			o = iter.next();
			if(o instanceof Subvertex){
				saida[JHekaton.saidas.get(((Subvertex)o).getId())] = 1;
			} else {
				saida[JHekaton.saidas.get(((Transition)o).getId())] = 1;
			}
			
			this.entradas.add(entrada);
			this.saidas.add(saida);
			
		}
		
	}
	
}