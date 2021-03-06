/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010
	
	F. Herrera (herrera@decsai.ugr.es)
    L. Sánchez (luciano@uniovi.es)
    J. Alcalá-Fdez (jalcala@decsai.ugr.es)
    S. García (sglopez@ujaen.es)
    A. Fernández (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/
  
**********************************************************************/

package keel.Algorithms.RE_SL_Methods.TSK_IRL;

import org.core.*;
import java.util.*;

class Tun_TSK {
  public double semilla;
  public long cont_soluciones;
  public long Gen, n_genes, n_reglas, n_generaciones;
  public int n_soluciones;

  public String fich_datos_chequeo, fich_datos_tst, fich_datos_val;
  public String fichero_conf, ruta_salida;
  public String fichero_br, fichero_reglas, fich_tra_obli, fich_tst_obli;
  public String datos_inter = "";
  public String cadenaReglas = "";

  public MiDataset tabla, tabla_tst, tabla_val;
  public BaseR base_reglas;
  public Adap_Tun fun_adap;
  public AG_Tun alg_gen;

  public Tun_TSK(String f_e) {
    fichero_conf = f_e;
  }

  private String Quita_blancos(String cadena) {
    StringTokenizer sT = new StringTokenizer(cadena, "\t ", false);
    return (sT.nextToken());
  }

  /** Reads the data of the configuration file */
  public void leer_conf() {
    int i, j;
    String cadenaEntrada, valor;
    double cruce, mutacion, a, b, porc_ind_ee;
    int long_poblacion, gen_ee;

    // we read the file in a String
    cadenaEntrada = Fichero.leeFichero(fichero_conf);
    StringTokenizer sT = new StringTokenizer(cadenaEntrada, "\n\r=", false);

    // we read the algorithm's name
    sT.nextToken();
    sT.nextToken();

    // we read the name of the training and test files
    sT.nextToken();
    valor = sT.nextToken();

    StringTokenizer ficheros = new StringTokenizer(valor, "\t ", false);
    fich_datos_chequeo = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    fich_datos_val = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    fich_datos_tst = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    //fichero_br = ( (ficheros.nextToken()).replace('\"', ' ')).trim();

    // we read the name of the output files
    sT.nextToken();
    valor = sT.nextToken();

    ficheros = new StringTokenizer(valor, "\t ", false);
    fich_tra_obli = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    fich_tst_obli = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    fichero_br = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    String aux = ( (ficheros.nextToken()).replace('\"', ' ')).trim(); //Fichero de informacion del anterior
    fichero_reglas = ( (ficheros.nextToken()).replace('\"', ' ')).trim();
    ruta_salida = fich_tst_obli.substring(0, fich_tst_obli.lastIndexOf('/') + 1);

	System.out.println("\n\nEntrando en Leer");

    // we read the seed of the random generator
    sT.nextToken();
    valor = sT.nextToken();
    semilla = Double.parseDouble(valor.trim());
    Randomize.setSeed( (long) semilla);

    for (i = 0; i < 17; i++) { //leo los 17 primeros parametros que son de los dos primeros métodos
      sT.nextToken(); //nombre parametro
      sT.nextToken(); //valor parametro
    }

    // we read the Number of Iterations
    sT.nextToken();
    valor = sT.nextToken();
    n_generaciones = Long.parseLong(valor.trim());

    // we read the Population Size
    sT.nextToken();
    valor = sT.nextToken();
    long_poblacion = Integer.parseInt(valor.trim());

    // we read the Parameter a
    sT.nextToken();
    valor = sT.nextToken();
    a = Double.parseDouble(valor.trim());

    // we read the Parameter b
    sT.nextToken();
    valor = sT.nextToken();
    b = Double.parseDouble(valor.trim());

	System.out.println("n_generaciones = " + n_generaciones + "  long_poblacion = " + long_poblacion + "  a = " + a + "  b = " + b);

    // we read the Cross Probability
    sT.nextToken();
    valor = sT.nextToken();
    cruce = Double.parseDouble(valor.trim());

    // we read the Mutation Probability
    sT.nextToken();
    valor = sT.nextToken();
    mutacion = Double.parseDouble(valor.trim());

    // we read the Rate of the Population to which the ES is applied
    sT.nextToken();
    valor = sT.nextToken();
    porc_ind_ee = Double.parseDouble(valor.trim());

    // we read the Evolutionary Strategy Iterations
    sT.nextToken();
    valor = sT.nextToken();
    gen_ee = Integer.parseInt(valor.trim());

	System.out.println("cruce = " + cruce + "  mutacion = " + mutacion + "  porc_ind_ee = " + porc_ind_ee + "  gen_ee = " + gen_ee);

    // we create all the objects
    tabla = new MiDataset(fich_datos_chequeo, false);
    if (tabla.salir == false) {
      tabla_val = new MiDataset(fich_datos_val, false);
      tabla_tst = new MiDataset(fich_datos_tst, false);
      base_reglas = new BaseR(fichero_br, tabla);
      fun_adap = new Adap_Tun(tabla, base_reglas);
      alg_gen = new AG_Tun(long_poblacion, cruce, mutacion, a, b, porc_ind_ee, gen_ee, fun_adap, base_reglas, tabla);
    }
  }

  public void run() {
    double ec, el, ec_tst, el_tst;

    /* We read the configutate file and we initialize the structures and variables */
    leer_conf();

    if (tabla.salir == false) {
      /* Generation of the initial population */
      Gen = 0;
	System.out.println ("\n\nInicializo AG");
      alg_gen.Initialize();

      /* Evaluation  of the initial population */
      alg_gen.Evaluate();
      Gen++;

      /* Main of the genetic algorithm */
      do {
        /* Interchange of the new and old population */
        alg_gen.Intercambio();

        /* Selection by means of Baker */
        alg_gen.Select();

        /* Crossover */
        alg_gen.Max_Min_Crossover();

        /* Mutation */
        alg_gen.Mutacion_No_Uniforme(Gen, n_generaciones);

		System.out.println ("\n\nEntro en EE");
        /* Estrategia de evolucion */
        alg_gen.Estrategia_Evolucion();
		System.out.println ("\n\nSalgo de EE");

        /* Elitist selection */
        alg_gen.Elitist();

        /* Evaluation of the current population */
        alg_gen.Evaluate();

        /* we increment the counter */
        Gen++;
		System.out.println ("Generacion = " + Gen);
      }
      while (Gen <= n_generaciones);

      /* we calcule the MSEs */
      fun_adap.Decodifica(alg_gen.solucion());
      fun_adap.Error_tra();
      ec = fun_adap.EC;
      el = fun_adap.EL;

      fun_adap.Error_tst(tabla_tst);
      ec_tst = fun_adap.EC;
      el_tst = fun_adap.EL;

      /* we write the RB */
      cadenaReglas = base_reglas.BRtoString();
      cadenaReglas += "\nMSEtra: " + ec + "  MLEtra: " + el;
      cadenaReglas += "\nMSEtst: " + ec_tst + "  MLEtst: " + el_tst;

      Fichero.escribeFichero(fichero_reglas, cadenaReglas);

      /* we write the obligatory output files*/
      String salida_tra = tabla.getCabecera();
      salida_tra += fun_adap.getSalidaObli(tabla_val);
      Fichero.escribeFichero(fich_tra_obli, salida_tra);

      String salida_tst = tabla_tst.getCabecera();
      salida_tst += fun_adap.getSalidaObli(tabla_tst);
      Fichero.escribeFichero(fich_tst_obli, salida_tst);

      /* we write the MSEs in specific files */
      Fichero.AnadirtoFichero(ruta_salida + "tun_tskcomunR.txt", "" + base_reglas.n_reglas + "\n");
      Fichero.AnadirtoFichero(ruta_salida + "tun_tskcomunTRA.txt", "" + ec + "\n");
      Fichero.AnadirtoFichero(ruta_salida + "tun_tskcomunTST.txt", "" + ec_tst + "\n");
    }
  }

}

