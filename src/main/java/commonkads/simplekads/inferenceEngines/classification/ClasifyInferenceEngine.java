package commonkads.simplekads.inferenceEngines.classification;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import commonkads.simplekads.ontologyAccessManager.classification.ClasifyOntologyAccess;

public class ClasifyInferenceEngine {
	private static List<String> differential = new ArrayList<String>();
	private static Map<String, Boolean> evidence = new HashMap<String, Boolean>();
	private static Map<String, List<String>> symptomsStructure = new HashMap<String, List<String>>();
	private static Map<String, Integer> symptomsCounter = new HashMap<String, Integer>();

	private static List<String> cover(String symptomName) {
		return ClasifyOntologyAccess.getDiseases(symptomName);
	}

	public static List<String> diagnose(String symptomName) {
		return diagnose(symptomName, 1);
	}

	private static String select() {
		// Always returns a disease, builds symptoms structure
		int lowerCount = -1;
		String lowerCountSymptom = "";
		for (Map.Entry<String, Integer> entry : symptomsCounter.entrySet()) {
			String symptom = entry.getKey();
			Integer count = entry.getValue();
			if (lowerCount == -1 || count < lowerCount) {
				lowerCountSymptom = symptom;
				lowerCount = count;
			}
		}

		for (Map.Entry<String, List<String>> entry : symptomsStructure
				.entrySet()) {
			String disease = entry.getKey();
			List<String> symptoms = entry.getValue();
			if (symptoms.contains(lowerCountSymptom)) {
				return disease;
			}
		}
		return differential.get(0);
	}

	private static String specify(String candidateDisease) {
		// May return null if no symptoms left
		int lowerCount = -1;
		String lowerCountSymptom = "";
		List<String> symptomList = symptomsStructure.get(candidateDisease);
		for (Iterator<String> iter = symptomList.iterator(); iter.hasNext();) {
			String symptom = iter.next();
			int count = symptomsCounter.get(symptom);
			if (lowerCount == -1 || count < lowerCount) {
				lowerCountSymptom = symptom;
				lowerCount = count;
			}
		}
		return lowerCountSymptom;
	}

	private static String obtain(String requestedObservable) {
		Boolean validResponse = false;
		while (!validResponse) {
			System.out.println("El paciente presenta el s�ntoma "
					+ requestedObservable + ": S/N");
			try {
				BufferedReader bufferRead = new BufferedReader(
						new InputStreamReader(System.in));
				String response = bufferRead.readLine();
				if (response.equalsIgnoreCase("S")) {
					validResponse = true;
					System.out
							.println("Se agreg� a la evidencia: Paciente presenta "
									+ requestedObservable);
					return "True";
				} else {
					if (response.equalsIgnoreCase("N")) {
						validResponse = true;
						System.out
								.println("Se agreg� a la evidencia: Paciente no presenta "
										+ requestedObservable);
						return "False";
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static boolean verify(String hypotesis) {
		if (symptomsStructure.containsKey(hypotesis)) {
			return true;
		} else {
			return false;
		}
	}

	public static List<String> diagnose(String symptomName, int resultLimit) {
		differential = cover(symptomName);
		buildSymptomStructure();
		int evidenceSize = evidence.size();
		Boolean noMoreEvidence = false;
		while (differential.size() > resultLimit && !noMoreEvidence) {
			String selectedDisease = select();
			String requestedObservable = specify(selectedDisease);
			if (requestedObservable != null) {
				String finding = obtain(requestedObservable);
				if (finding != null) {
					if (finding.equals("True")) {
						evidence.put(requestedObservable, true);
						discardSymptom(requestedObservable);
					} else {
						evidence.put(requestedObservable, false);
						discardDiseaseBySymptom(requestedObservable);
					}

				}
			}
			for (Iterator<String> iter = differential.iterator(); iter
					.hasNext();) {
				String hypothesis = iter.next();
				if (!verify(hypothesis)) {
					iter.remove();
					System.out
							.println("Se descart� la hipotesis " + hypothesis);
				}
			}
			if (evidenceSize == evidence.size()) {
				// No more evidence found
				noMoreEvidence = true;
			}
			evidenceSize = evidence.size();
		}
		return differential;
	}

	// Auxiliar Functions
	private static void discardSymptom(String symptom) {
		for (Map.Entry<String, List<String>> entry : symptomsStructure
				.entrySet()) {
			String disease = entry.getKey();
			List<String> symptoms = entry.getValue();
			int index = symptoms.indexOf(symptom);
			if (index != -1) {
				symptoms.remove(index);
				System.out.println("Se marc� como validado el sintoma "
						+ symptom);
			}
		}
	}

	private static void discardDiseaseBySymptom(String symptom) {
		List<String> delete = new ArrayList<String>();
		for (Map.Entry<String, List<String>> entry : symptomsStructure
				.entrySet()) {
			String disease = entry.getKey();
			List<String> symptoms = entry.getValue();
			if (symptoms.contains(symptom)) {
				delete.add(disease);
			}
		}
		for (Iterator<String> iter = delete.iterator(); iter.hasNext();) {
			String disease = iter.next();
			symptomsStructure.remove(disease);
		}

	}

	private static void buildSymptomStructure() {
		List<String> symptoms = new ArrayList<String>();
		if (symptomsStructure.size() == 0) {
			// Load symptoms once and build symtoms counter array
			for (Iterator<String> iter = differential.iterator(); iter
					.hasNext();) {
				String hypothesis = iter.next();
				symptoms = ClasifyOntologyAccess.getSymptoms(hypothesis);
				symptomsStructure.put(hypothesis, symptoms);
				for (Iterator<String> jter = symptoms.iterator(); jter
						.hasNext();) {
					String symptom = jter.next();
					if (symptomsCounter.containsKey(symptom)) {
						symptomsCounter.put(symptom,
								symptomsCounter.get(symptom) + 1);
					} else {
						symptomsCounter.put(symptom, 1);
					}
				}
			}
		}
	}

	// Debug Functions
	private static void printMultiMap(Map<String, List<String>> multimap) {
		for (Map.Entry<String, List<String>> entry : multimap.entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			System.out.println("Key = " + key);
			System.out.println("Values = " + values);
		}
	}

	private static void printMap(Map<String, Integer> map) {
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			System.out.println("Key = " + key);
			System.out.println("Value = " + value);
		}
	}
}
