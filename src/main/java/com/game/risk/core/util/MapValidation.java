package com.game.risk.core.util;

import com.game.risk.core.CountriesGraph;
import com.game.risk.model.Country;

import java.io.*;
import java.util.*;

/**
 * Map Validation class is implemented for data validation before loading it
 * with map file parser
 *
 * @author Vida Abdollahi
 * @author sohrab_singh
 */
public class MapValidation {

	/**
	 * Array List to store continents which will be found in [Continents] tag
	 */
	private ArrayList<String> continentInContinent;

	/**
	 * Array List to store continents which will be found in [Territories] tag
	 */
	private ArrayList<String> continentInTerritory;

	/**
	 * Hash Map to store each country and its adjacent countries as an array list
	 */
	private HashMap<String, ArrayList<String>> Countries;

	/**
	 * This parameter will be true if the given graph is a connected graph
	 */
	private boolean isConnectedGraph;

	/**
	 * Map validation constructor
	 */

	public MapValidation() {
		continentInContinent = new ArrayList<>();
		continentInTerritory = new ArrayList<>();
		Countries = new HashMap<String, ArrayList<String>>();
	}

	/**
	 * Method to check whether file is valid or not.
	 *
	 * @param filename
	 *            file name to be validated.
	 * @return true if file is valid otherwise false.
	 * @throws IOException
	 *             input out exception
	 */
	public boolean validateFile(String filename) throws IOException {
		if (filename == null)
			return false;
		File file = new File(filename);
		System.out.println(file.getAbsolutePath());
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		boolean isValid = true;
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		// Check to see whether all the tags are defined in the file
		isValid = checkMandatoryTags(str);

		while (true) {

			line = bufferedReader.readLine();

			if (line == null) {
				break;
			}
			// Check for continents format
			line = checkContinentFormat(bufferedReader, line);

			// Check for countries format
			checkCountriesFormat(bufferedReader, line);

		}

		// Check to see whether number of continents in [Continents] tag is equeal with
		// number of continents in [Territories tag]
		if (continentInContinent.size() != continentInTerritory.size()) {
			System.out.println(
					"* Number of continents in [Continents] tag is not equal with number of continents in [Territories] tag");

		}

		// make sure that all continents have at least one country
		for (Object continent : continentInContinent) {
			if (!continentInTerritory.contains(continent)) {
				System.out.println(continent + ": does not have any country");
			}
		}
		// check to make sure that if country X has country Y as it adjacent then,
		// country Y has country X as its adjacent as well
		for (String country : Countries.keySet()) {

			for (Object adjlist : Countries.get(country)) {
				if ((Countries.get(adjlist.toString()) != null)) {
					if (Countries.get(adjlist.toString()).contains(country)) {
						// System.out.println("*" + adjlist + ": Adjacents are OK");
					}

				} else {
					System.out.println("*" + adjlist + ": Undefined Country ");
				}

			}

			bufferedReader.close();

		}
		return isValid;
	}

	/**
	 * Check continents format.
	 *
	 * @param bufferedReader
	 *            buffer reader to read file
	 * @param line
	 *            previous line
	 * @return current read line
	 * @throws IOException
	 */
	public String checkContinentFormat(BufferedReader bufferedReader, String line) throws IOException {
		if (line.startsWith("[Continents]")) {
			while ((!(line = bufferedReader.readLine()).startsWith("[Territories]"))) {
				if (!line.isEmpty()) {
					String pattern = "[^,;=]+=[0-9]+";

					if (!line.matches(pattern)) {
						System.out.println("* " + line + ": Invalid format for a continent ");
						return "Invalid";

					} else {
						String[] splitLine = line.split("=");
						if (!continentInContinent.contains(splitLine[0])) {
							continentInContinent.add(splitLine[0]);

						} else {
							System.out.println("*" + splitLine[0] + " is defined more than one time");
						}
					}
				}
			}
		}
		return line;
	}

	/**
	 * Check countries format.
	 *
	 * @param bufferedReader
	 *            buffer reader to read file
	 * @param line
	 *            current read line
	 * @return true if format is valid otherwise false
	 * @throws IOException
	 */
	public boolean checkCountriesFormat(BufferedReader bufferedReader, String line) throws IOException {
		boolean isValid = true;
		if (line.startsWith("[Territories]")) {
			while ((line = bufferedReader.readLine()) != null) {
				if (!line.isEmpty()) {
					String pattern = "[^;,]+,[0-9]+,[0-9]+,[^;,]+,[^;,]+(,[^;,]+)*";

					if (!line.matches(pattern)) {
						System.out.println("* " + line + ": Invalid format for a territory ");
						isValid = false;

					} else {
						String[] split = line.split(",");

						if (!continentInTerritory.contains(split[3])) {
							continentInTerritory.add(split[3]);

						}

						if (!Countries.containsKey(split[0])) {

							ArrayList<String> arrayList = new ArrayList<String>();
							for (int i = 4; i < split.length; i++) {
								arrayList.add(split[i]);

							}
							Countries.put(split[0], arrayList);

						} else {
							System.out.println("* " + split[0] + ": is defined more than one time");

						}

					}

				}

			}

		}
		return isValid;
	}

	/**
	 * Check mandatory tags.
	 *
	 * @param str
	 *            whole file
	 * @return true if present otherwise false
	 */
	public boolean checkMandatoryTags(String str) {
		boolean isValid = true;

		if (!str.contains("[Map]")) {
			System.out.println("No [Map] tag is defined");
			isValid = false;
		}
		if (!str.contains("[Territories]")) {
			System.out.println("No [Territories] tag is defined");
			isValid = false;
		}
		if (!str.contains("[Continents]")) {
			System.out.println("No [Continents] tag is defined");
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Check to see whether the garph is a connected graph (DFS)
	 * 
	 * @param countriesGraph
	 * @return true if it is a connected graph
	 *
	 */
	public boolean checkConnectedGraph(CountriesGraph countriesGraph) {

		HashMap<Country, LinkedList<Country>> countries = countriesGraph.getAdjListHashMap();
		Country parent = (Country) countries.keySet().iterator().next();

		Stack stack = new Stack();
		stack.push(parent);
		parent.setVisited(true);

		while (!stack.isEmpty()) {

			String rootCountry = (String) stack.peek();

			for (Country key : countries.keySet()) {
				for (Country country : countries.get(key)) {

					if (country.isVisited() == false) {
						stack.push(country);
						country.setVisited(true);
					} else {
						stack.pop();
					}
				}

			}
		}

		for (Country country : countries.keySet()) {
			if (country.isVisited()) {
				isConnectedGraph = true;
			} else {
				isConnectedGraph = false;
			}
		}

		return isConnectedGraph;
	}

}