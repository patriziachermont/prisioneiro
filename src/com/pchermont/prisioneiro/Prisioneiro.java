package com.pchermont.prisioneiro;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe de representação do indivíduo prisioneiro.
 * @author Patrizia Chermont
 *
 */
public class Prisioneiro {
	
	/**
	 * Genes do indivíduo na forma booleana. Cooperou = true, Delatou = false.
	 */
	private boolean genes[];
	
	/**
	 * Gerador de Números Aleatórios.
	 */
	private Random rng;
	
	/**
	 * Último fitness calculado do indivíduo.
	 */
	private double fitness;
	
	/**
	 * Número aleatório para ordenação embaralhada.
	 */
	private int iRandom;
	
	/**
	 * Método construtor da classe.
	 * Inicializa um novo prisioneiro com uma cadeia aleatória de genes. 
	 */
	public Prisioneiro(){
		genes = new boolean[GAConfig.nGenes];
		rng = new Random();
		
		for (int i = 0; i < GAConfig.nGenes; i++){
			genes[i] = rng.nextBoolean(); 
		}
		
		iRandom = rng.nextInt();
		setFitness(0);
	}
	
	/**
	 * Método construtor da classe.
	 * Inicializa um novo prisioneiro com uma cadeia definida de genes.
	 * @param genes Cadeia booleana de cooperações (true) e delações (false).
	 */
	public Prisioneiro (boolean[] genes){
		this.genes = genes;
		rng = new Random();
		iRandom = rng.nextInt();
		setFitness(0);
	}
	
	/**
	 * Tempo de cadeia que o prisioneiro é sentenciado quando confrontado com
	 * outro prisioneiro.
	 * @param outro Comparsa a ser interrogado junto com o Prisioneiro.
	 * @return O tempo de cadeia sentenciado.
	 */
	public double tempo(Prisioneiro outro){
		
		double tempo = 0;
		
		for (int i = 0; i < GAConfig.nGenes; i++){
			if (this.cooperou(i) && outro.cooperou(i)){
				tempo += GAConfig.tCalados;
			} else if (this.cooperou(i) && !outro.cooperou(i)){
				tempo += GAConfig.tDelatado;
			} else if (!this.cooperou(i) && outro.cooperou(i)){
				tempo += GAConfig.tDelator;
			} else {
				tempo += GAConfig.tReciproco;
			}
		}

		return tempo;
	}
	
	/**
	 * Função de aptidão do indivíduo. Neste algoritmo, o fitness é calculado
	 * tomando por base o tempo de cadeia e o número de cooperações seguidas.
	 * Para efeitos de ordenação, tomou-se como fórmula a expressão:
	 * fitness = 1/tempo + deltas, na qual deltas é a premiação por cooperações
	 * consecutivas.
	 * @param outro Comparsa interrogado
	 * @return Aptidão do indivíduo em ter menos tempo de cadeia.
	 */
	public double fitness(Prisioneiro outro){
		
		double deltas	= 0;
		int seguidos	= 0;
		double tempo = tempo(outro);
		
		for (int i = 0; i < GAConfig.nGenes; i++){
			if (cooperou(i)) {
				seguidos++;
				if (seguidos == GAConfig.nSeguidos){
					seguidos = 0;
					deltas = deltas + GAConfig.delta;
				}
			} else {
				seguidos = 0;
			}
		}
		
		// Pequeno hack para evitar divisão por zero.
		// Improvável, mas não impossível.
		if (tempo == 0) tempo = 0.000001;
		
		setFitness(1/tempo + deltas);		
		return getFitness();		
	}
	
	/**
	 * Indica se o indivíduo cooperou o delatou com a informação.
	 * @param i Posição da cadeia de genes.
	 * @return true, se o indivíduo cooperou, ou false, caso contrário.
	 */
	public boolean cooperou(int i){
		return gene(i);
	}
	
	/**
	 * Gene do indivíduo.
	 * @param i Posição da cadeia de genes.
	 * @return Valor do gene (true ou false).
	 */
	public boolean gene(int i){
		return genes[i];
	}
	
	/**
	 * Gera filhos com um parceiro. Será utilizado apenas um ponto de
	 * cruzamento na metade da cadeia de genes. O método gera 4 novos filhos,
	 * sendo duas duplas de gêmeos.
	 * @param parceiro Parceiro utilizado no cruzamento de genes.
	 * @return Lista de 4 novos filhos, sendo duas duplas de gêmeos.
	 */
	public List<Prisioneiro> gerar(Prisioneiro parceiro){
		
		boolean[] genes1 = new boolean[GAConfig.nGenes];
		boolean[] genes2 = new boolean[GAConfig.nGenes];
		boolean[] genes3 = new boolean[GAConfig.nGenes];
		boolean[] genes4 = new boolean[GAConfig.nGenes];
		
		int metade = GAConfig.nGenes / 2;
		
		List<Prisioneiro> filhos = new ArrayList<Prisioneiro>(2);
		
		for (int i = 0; i < GAConfig.nGenes; i++){			
			// Crossover entre os genótipos
			genes1[i] = (i < metade)? this.gene(i) : parceiro.gene(i);
			genes2[i] = (i < metade)? parceiro.gene(i) : this.gene(i);
			genes3[i] = (i < metade)? this.gene(i) : parceiro.gene(i);
			genes4[i] = (i < metade)? parceiro.gene(i) : this.gene(i);	
			
			//Aplicacao de mutação no gene
			if (rng.nextDouble() < GAConfig.pMutacao) genes1[i] = !genes1[i];
			if (rng.nextDouble() < GAConfig.pMutacao) genes2[i] = !genes2[i];
			if (rng.nextDouble() < GAConfig.pMutacao) genes3[i] = !genes3[i];
			if (rng.nextDouble() < GAConfig.pMutacao) genes4[i] = !genes4[i];						
		}
		
		filhos.add(new Prisioneiro(genes1));
		filhos.add(new Prisioneiro(genes3));
		filhos.add(new Prisioneiro(genes2));
		filhos.add(new Prisioneiro(genes4));
		
		return filhos;
	}
	
	/**
	 * Realiza uma disputa entre dois indivíduos. 
	 * @param outro Comparsa com qual será realizada a disputa.
	 * @return O indivíduo vencedor (o comparsa ou o prórpio indivíduo).
	 */
	public Prisioneiro disputar(Prisioneiro outro){
		boolean venci = (this.fitness(outro) > outro.fitness(this)); 
//		System.out.println(
//				this + " X " + outro +
//				" <=> " +
//				this.tempo(outro) + " X " + outro.tempo(this) +
//				"(" + this.getFitness() + " <=> " + outro.getFitness() + ")");
		return venci? this : outro;
	}
	
	/**
	 * Sobrescrita do método toString para realizar relatório.
	 * @return A cadeia de genes do indivíduo.
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		double c = 0;
		for (int i = 0; i < GAConfig.nGenes; i++){
			if(gene(i)){
				sb.append("C");
				c++;
			} else {
				sb.append("D");
			}
		}
		sb.append(": C="+c/GAConfig.nGenes);
		sb.append(", F="+getFitness());
		return sb.toString();
	}

	/**
	 * Método get do número aleatório para ordenação embaralhada.
	 * @return o número aleatório do Indivíduo.
	 */
	public int getRandom() {
		return iRandom;
	}

	/**
	 * Método set do Fitness do indivíduo.
	 * @param fitness Aptidão do indivíduo.
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;	
	}
	
	/**
	 * Método GET do fitness do indivíduo.
	 * @return Aptdão do indivíduo
	 */
	public double getFitness(){
		return this.fitness;
	}
	
}
