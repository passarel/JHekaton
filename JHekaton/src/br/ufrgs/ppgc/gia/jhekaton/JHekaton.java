package br.ufrgs.ppgc.gia.jhekaton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	public static void main(String[] args) {
		
		// Arquivo Origem
		File file = new File("src/model.uml");
		
		JAXBContext jaxbContext;
		try {
			// Instancia o Parser
			jaxbContext = JAXBContext.newInstance(Model.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			//Parseia a entrada
			Model stateMachineDiagram = (Model) jaxbUnmarshaller.unmarshal(file);
			
			int countEntradas = 0, countSaidas = 0;
			
			JHekaton.entradas = new HashMap<String, Integer>();
			JHekaton.saidas = new HashMap<String, Integer>();
			
			// Itera sobre os estados
			for(Subvertex sub : stateMachineDiagram.getPackagedElement().getRegion().getSubvertex()){
				// Conta os estados como entradas, descontando o "estado final"
				if(!sub.getType().equalsIgnoreCase(StateType.FINAL.getUmlType())){
					JHekaton.entradas.put(sub.getId(), countEntradas);
					System.out.println("Entrada:"+countEntradas+":"+sub.getId()); 
					countEntradas++;
				}
				
				// Conta os estados como saída, descontando o "estado inicial"
				if(!sub.getType().equalsIgnoreCase(StateType.INITIAL.getUmlType())){
					JHekaton.saidas.put(sub.getId(), countSaidas);
					System.out.println("Saída:"+countSaidas+":"+sub.getId());
					countSaidas++;
				}
			}
			
			// Itera sobre as transições
			for(Transition t : stateMachineDiagram.getPackagedElement().getRegion().getTransition()){
				JHekaton.entradas.put(t.getId(), countEntradas);
				System.out.println("Entrada:"+countEntradas+":"+t.getId());
				countEntradas++;
			}
			
			System.out.println("Topologia da Rede: "+countEntradas +":" + stateMachineDiagram.getPackagedElement().getRegion().getTransition().size() + ":"+countSaidas);

			// Gera o Arquivo de treinamento - Sequencia de Supervisao
			List<Object> conjuntoTreinamento = JHekaton.geraConjuntoTreinamento(stateMachineDiagram.getPackagedElement().getRegion());
			System.out.println();
			Instancia inst = new Instancia(countEntradas, countSaidas);
			inst.construir(conjuntoTreinamento);
			
			// Criação da rede, com 3 camadas			
			ElmanPattern pattern = new ElmanPattern();
			pattern.setActivationFunction(new ActivationSigmoid());
			// Cria rede com camada de entrada dimensionada pela contagem
			pattern.setInputNeurons(countEntradas);
			// quantidade de camadas ocultas segue o número de transições
			pattern.addHiddenLayer(stateMachineDiagram.getPackagedElement().getRegion().getTransition().size());
			// ... camada de saída dimensionada pela contagem
			pattern.setOutputNeurons(countSaidas);
			BasicNetwork rede = (BasicNetwork)pattern.generate();
			

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
				System.out.println("Epoch #" + epoch	+ " Error:" + trainMain.getError());
				epoch++;
			}
			trainMain.finishTraining();
			System.out.println("Erro Final apos treinamento: "+trainMain.getError());
			
			// teste 
			System.out.println("Caso de teste:");
			for(double i : entradasTreinamento[TESTE]){
				System.out.print(i+"\t");
			}
			System.out.println();
			System.out.println("Resultado esperado:");
			
			for(double i : saidasTreinamento[TESTE]){
				System.out.print(i+"\t");
			}

			System.out.println();
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
			System.out.println();
			System.out.println("Final: "+index);
			System.out.println("Resultado real:");
			for(double i : result.getData()){
				System.out.print(i+"\t");
			}
			System.out.println();
			
//			MLDataSet trainingSet1 = new BasicMLDataSet(ENTRADAS1,SAIDAS1) ;
//			
//			CalculateScore score1 = new TrainingSetScore(trainingSet1);
//
//			final MLTrain trainMain1 = new Backpropagation(rede, trainingSet, 0.0000000001, 0.001);
//			final MLTrain trainAlt1 = new NeuralSimulatedAnnealing(rede, score1, 10, 2, 100);
//
//			final StopTrainingStrategy stop1 = new StopTrainingStrategy();
//			trainMain1.addStrategy(new Greedy());
//			trainMain1.addStrategy(new HybridStrategy(trainAlt1));
//			trainMain1.addStrategy(stop1);
//
//			int epoch1 = 0;
//			while (!stop1.shouldStop()) {
//				// cada iteração é uma época
//				trainMain1.iteration();
//				System.out.println("Epoch #" + epoch1 + " Error:" + trainMain1.getError());
//				epoch1++;
//			}
//			trainMain1.finishTraining();
//			System.out.println("Erro Final apos treinamento: "+trainMain1.getError());
//			
//			// teste 
//			System.out.println("Caso de teste:");
//			for(double i : ENTRADAS1[TESTE]){
//				System.out.print(i+"\t");
//			}
//			System.out.println();
//			System.out.println("Resultado esperado:");
//			
//			for(double i : SAIDAS1[TESTE]){
//				System.out.print(i+"\t");
//			}
//
//			System.out.println();
//			double[] teste2 = ENTRADAS1[TESTE]; // Instancia 1;
//			MLData testeSet1 = new BasicMLData(teste2) ;
//			MLData result1 = rede.compute(testeSet1);
//			double maior1 = -1d;
//			int index1 = -1;
//			for(int i = 0 ; i < result1.getData().length; i++){
//				if(maior1 < result1.getData()[i]){
//					maior1 = result1.getData()[i];
//					index1 = i;
//				}
//			}
//			System.out.println();
//			System.out.println("Final: "+index1);
//			System.out.println("Resultado real:");
//			for(double i : result1.getData()){
//				System.out.print(i+"\t");
//			}

			
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private static List<Object> geraConjuntoTreinamento(Region diagram){
		System.out.println("==== Caminhamento =====");
		List<Object> result = new ArrayList<Object>();
		Subvertex state = XmiUtil.getStatesByType(diagram, StateType.INITIAL).get(0);
		while(!state.getType().equalsIgnoreCase(StateType.FINAL.getUmlType())){
			result.add(state);
			System.out.println("Estado: "+state.getName()+":"+state.getId());
			Transition t =  XmiUtil.getTransitionsFromState(diagram, state.getId()).get(0);
			result.add(t);
			System.out.println("Transicao: "+t.getName()+":"+t.getId());
			state = XmiUtil.getStateById(diagram, t.getTarget());
		}
		result.add(state);
		System.out.println("Estado: "+state.getName()+":"+state.getId());
		return result;
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
		this.entradas = new ArrayList<double[]>();
		this.saidas   = new ArrayList<double[]>();
			
		Iterator iter = caminho.iterator();		
		Object o = iter.next();
		while(iter.hasNext()){
			
			double[] entrada = new double[this.countEntradas];
			double[] saida = new double[this.countSaidas];
			
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