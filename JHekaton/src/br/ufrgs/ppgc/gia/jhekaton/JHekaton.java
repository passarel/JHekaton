package br.ufrgs.ppgc.gia.jhekaton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	// Matriz de entradas
	private static final double[][] ENTRADAS = new double[][]{
//		{Ir,C1,C2,F1,F2,II,S0,S1,S2}, Rótulos 
		{1d,0d,0d,0d,0d,1d,0d,0d,0d}, // Instancia 1
		{0d,1d,0d,0d,0d,0d,1d,0d,0d}, // Instancia 2
		{0d,0d,1d,0d,0d,0d,1d,0d,0d}, // Instancia 3
		{0d,0d,0d,1d,0d,0d,0d,1d,0d}, // Instancia 4
		{0d,0d,0d,0d,1d,0d,0d,0d,1d}  // Instancia 5
	};
				
				// Matriz de saídas
	private static final double[][] SAIDAS = new double[][]{
//		{S0,S1,S2,FF}, Rótulos
		{1d,0d,0d,0d}, // Instancia 1
		{0d,1d,0d,0d}, // Instancia 2
		{0d,0d,1d,0d}, // Instancia 3
		{0d,0d,0d,1d}, // Instancia 4
		{0d,0d,0d,1d}  // Instancia 5
	};
	
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
			
			// Itera sobre os estados
			for(Subvertex sub : stateMachineDiagram.getPackagedElement().getRegion().getSubvertex()){
				System.out.println("Estado: "+sub.getName()+"-"+sub.getType());
				
				// Conta os estados como entradas, descontando o "estado final"
				if(!sub.getType().equalsIgnoreCase(StateType.FINAL.getUmlType())){
					countEntradas++;
				}
				
				// Conta os estados como saída, descontando o "estado inicial"
				if(!sub.getType().equalsIgnoreCase(StateType.INITIAL.getUmlType())){
					countSaidas++;
				}
			}
			
			// Itera sobre as transições
			for(Transition t : stateMachineDiagram.getPackagedElement().getRegion().getTransition()){
				System.out.println("Transicao: "+t.getName());
				countEntradas++;
			}
			System.out.println(countEntradas + ":"+countSaidas);

			// Criação da rede, com 2 camadas			
			ElmanPattern pattern = new ElmanPattern();
			pattern.setActivationFunction(new ActivationSigmoid());
			// Cria rede com camada de entrada dimensionada pela contagem
			pattern.setInputNeurons(countEntradas);
			// quantidade de camadas ocultas segue o número de transições
			pattern.addHiddenLayer(stateMachineDiagram.getPackagedElement().getRegion().getTransition().size());
			// ... camada de saída dimensionada pela contagem
			pattern.setOutputNeurons(countSaidas);
			BasicNetwork rede = (BasicNetwork)pattern.generate();
			
			
//			// Criação da rede, com 2 camadas
//			JordanPattern pattern = new JordanPattern();
//			pattern.setActivationFunction(new ActivationSigmoid());
//			// Cria rede com camada de entrada dimensionada pela contagem 
//			pattern.setInputNeurons(countEntradas);
//
//			pattern.addHiddenLayer(5);
//			
//			// ... camada de saída dimensionada pela contagem
//			pattern.setOutputNeurons(countSaidas);
//			BasicNetwork rede = (BasicNetwork) pattern.generate();
			
			MLDataSet trainingSet = new BasicMLDataSet(ENTRADAS,SAIDAS) ;
			
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
			for(double i : ENTRADAS[TESTE]){
				System.out.print(i+"\t");
			}
			System.out.println();
			System.out.println("Resultado esperado:");
			
			for(double i : SAIDAS[TESTE]){
				System.out.print(i+"\t");
			}

			System.out.println();
			double[] teste1 = ENTRADAS[TESTE]; // Instancia 1;
			MLData testeSet = new BasicMLData(teste1) ;
			MLData result = rede.compute(testeSet);
			System.out.println("Resultado arredondado:");
			double maior = 0d;
			int index = -1;
			for(int i = 0 ; i < result.getData().length; i++){
				if(maior < result.getData()[i]){
					maior = result.getData()[i];
					index = i;
				}
				System.out.print(Math.round(result.getData()[i])+"\t");
			}
			System.out.println();
			System.out.println("Final: "+index);
			System.out.println("Resultado real:");
			for(double i : result.getData()){
				System.out.print(i+"\t");
			}

			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private List<Instancia> geraConjuntoTreinamento(Region diagram){
		List<Instancia> result = new ArrayList<Instancia>();
		Subvertex initialState = XmiUtil.getStatesByType(diagram, StateType.INITIAL).get(0);
		return result;
	}
	
}

class Instancia {
	
	private double[][] entradas;
	private double[][] saidas;

	public double[][] getEntradas() {
		return entradas;
	}
	public void setEntradas(double[][] entradas) {
		this.entradas = entradas;
	}
	public double[][] getSaidas() {
		return saidas;
	}
	public void setSaidas(double[][] saidas) {
		this.saidas = saidas;
	}
}