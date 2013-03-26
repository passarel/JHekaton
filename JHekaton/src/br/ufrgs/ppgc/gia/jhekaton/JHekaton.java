package br.ufrgs.ppgc.gia.jhekaton;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.specific.BiPolarNeuralData;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Greedy;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.bam.BAM;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.NeuralDataMapping;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.CalculateScore;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.JordanPattern;

import br.ufrgs.ppgc.gia.jhekaton.xml.Model;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Subvertex;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Transition;

/**
 * Classe que contém o método de inicialização da aplicação
 * @author Igor Montezano - imsantos@inf.ufrgs.br
 *
 */
public class JHekaton {

	public static void main(String[] args) {
		
		File file = new File("src/model.uml");
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(Model.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Model customer = (Model) jaxbUnmarshaller.unmarshal(file);
			
			
			int countEntradas = 0, countSaidas = 0;
			for(Subvertex sub : customer.getPackagedElement().getRegion().getSubvertex()){
				System.out.println("Estado: "+sub.getName()+"-"+sub.getType());
				if(!sub.getType().equalsIgnoreCase("uml:FinalState")){
					countEntradas++;
				}
				if(!sub.getType().equalsIgnoreCase("uml:Pseudostate")){
					countSaidas++;
				}
			}
			for(Transition t : customer.getPackagedElement().getRegion().getTransition()){
				System.out.println("Transicao: "+t.getName());
				countEntradas++;
			}
			System.out.println(countEntradas + ":"+countSaidas);
			
			// Criação da rede, com 2 camadas
			JordanPattern pattern = new JordanPattern();
			pattern.setActivationFunction(new ActivationSigmoid());
			pattern.setInputNeurons(countEntradas);
			pattern.addHiddenLayer(1);
			pattern.setOutputNeurons(countSaidas);
			BasicNetwork rede = (BasicNetwork) pattern.generate();
			
			// train the neural network
			BiPolarNeuralData data1 = new BiPolarNeuralData(new boolean[]{true,false,false,false,false,true,false,false,false,false});
			BiPolarNeuralData data2 = new BiPolarNeuralData(new boolean[]{true,false,false,false});
			
			MLDataSet trainingSet;
			
			CalculateScore score = new TrainingSetScore(trainingSet);
			final MLTrain trainAlt = new NeuralSimulatedAnnealing(rede, score, 10, 2, 100);

			final MLTrain trainMain = new Backpropagation(rede, trainingSet,0.00001, 0.0);

			final StopTrainingStrategy stop = new StopTrainingStrategy();
			trainMain.addStrategy(new Greedy());
			trainMain.addStrategy(new HybridStrategy(trainAlt));
			//trainMain.addStrategy(stop);

			int epoch = 0;
			while (!stop.shouldStop()) {
				trainMain.iteration();
				System.out.println("Epoch #" + epoch	+ " Error:" + trainMain.getError());
				epoch++;
			}
			trainMain.getError();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
}

/* Exemplo de NN */
class XORHelloWorld {

	/**
	 * The input necessary for XOR.
	 */
	public static double XOR_INPUT[][] = { { 0.0, 0.0 }, { 1.0, 0.0 },
			{ 0.0, 1.0 }, { 1.0, 1.0 } };

	/**
	 * The ideal data necessary for XOR.
	 */
	public static double XOR_IDEAL[][] = { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };
	
	/**
	 * The main method.
	 * @param args No arguments are used.
	 */
	public static void main(final String args[]) {
		
		// create a neural network, without using a factory
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,2));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,3));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();

		// create training data
		MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);
		
		// train the neural network
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

		int epoch = 1;

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01);

		// test the neural network
		System.out.println("Neural Network Results:");
		for(MLDataPair pair: trainingSet ) {
			final MLData output = network.compute(pair.getInput());
			System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
					+ ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
		}
		
		Encog.getInstance().shutdown();
	}
}

