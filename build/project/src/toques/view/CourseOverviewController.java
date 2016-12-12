package toques.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import toques.MainApp;
import toques.view.course;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

public class CourseOverviewController {
	@FXML
	private ListView<String> courseList;
	@FXML
	private Label status;
	@FXML
	private CheckBox two;
	@FXML
	private CheckBox three;
	@FXML
	private CheckBox four;
	@FXML
	private CheckBox five;
	@FXML
	private CheckBox six;
	@FXML
	private CheckBox seven;
	@FXML
	private CheckBox eight;
	@FXML
	private ProgressBar statusBar;
	private MainApp mainApp;

	public CourseOverviewController() {
	}

	@FXML
	private void initialize() {

	}
	public void changeStatusLabel(String s)
	{
		status.setText(s);
	}
	public void changeProgressBar(double num)
	{
		statusBar.setProgress(num);
	}
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
	
	public ArrayList<String> getWantedRequirements() {
		ArrayList<String> reqs = new ArrayList<String>();
		if (two.isSelected()) {
			reqs.add(" II ");
		}
		if (three.isSelected()) {
			reqs.add(" III ");
		}
		if (four.isSelected()) {
			reqs.add(" IV ");
		}
		if (five.isSelected()) {
			reqs.add(" V ");
		}
		if (six.isSelected()) {
			reqs.add(" VI ");
		}
		if (seven.isSelected()) {
			reqs.add(" VII ");
		}
		if (eight.isSelected()) {
			reqs.add(" VIII ");
		}
		return reqs;

	}
	public void handleStart() throws FileNotFoundException,
			ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		/**
		Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
			
		@Override
		protected ObservableList<String> call() throws Exception{
		
		//status.setText("Started");
		ArrayList<String> reqs = getWantedRequirements();
		ArrayList<String> courses = getListOfCourses("http://catalogue.uci.edu/informationforadmittedstudents/requirementsforabachelorsdegree/");
		String filename = "savedHashSetCourses.dat";
		Path courseFile = Paths.get(System.getProperty("user.home"),
				"savedHashSetCourses.dat");
		boolean haveFile = Files.exists(courseFile);
		ArrayList<String> goodCourses = new ArrayList<String>();
		if (haveFile) {
			//status.setText("Loading courses from Catalogue");
			goodCourses = giveGoodCourses(courseFile, reqs, courses);
		} else {
			//status.setText("Grabbing courses from Catalogue");
			goodCourses = giveGoodCourses(reqs, courses, courseFile);
		}
		//status.setText("Sorting results");
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(goodCourses);
		goodCourses.clear();
		goodCourses.addAll(hs);
		Collections.sort(goodCourses);
		ObservableList<String> sortedCourses = FXCollections
				.observableArrayList();
		sortedCourses.addAll(goodCourses);
		//status.setText("Finished");
		return sortedCourses;
		}
		};
		/*
		Thread th  = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		task.run();
		courseList.setItems(task.get());
		***/
		courseList.setItems(mainApp.giveCourses(two,three,four,five,six,seven,eight));
		//status.setText("Finished");
	}

	public static ArrayList<String> giveGoodCourses(Path courseFile,
			ArrayList<String> requirements, ArrayList<String> courses)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		ArrayList<course> courses2 = loadCoursesFile(courseFile);
		ArrayList gCourses = new ArrayList();
		for (int i = 0; i < courses2.size(); i++) {
			if (((course) (courses2.get(i))).GEs() != "") {
				if (checkCourseFulfillsRequirements(requirements,
						((course) (courses2.get(i))).GEs())) {
					gCourses.add(((course) (courses2.get(i))).giveTitle());
				}
			}
		}
		return gCourses;
	}

	public static ArrayList<String> giveGoodCourses(ArrayList requirements,
			ArrayList courses, Path filename) throws FileNotFoundException,
			ClassNotFoundException, IOException {
		ArrayList courses2 = new ArrayList();
		ArrayList gCourses = new ArrayList();
		Set<course> cl = new HashSet<course>();
		for (int i = 0; i < courses.size(); i++) {
			Document d = getCoursePage((String) (courses.get(i)));
			String GEs = GEs_fulfilled(d);
			course courset = new course((String) (courses.get(i)), GEs);
			cl.add(courset);
			courses2.add(courset);

			if (((course) (courses2.get(i))).GEs() != "") {
				if (checkCourseFulfillsRequirements(requirements,
						((course) (courses2.get(i))).GEs())) {
					gCourses.add(((course) (courses2.get(i))).giveTitle());
				}
			}
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				filename.toFile()));
		oos.writeObject(cl);
		oos.close();
		return gCourses;
	}

	public static ArrayList<course> loadCoursesFile(Path nameOfFile)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		Set<course> cl = new HashSet<course>();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				nameOfFile.toFile()));
		Set<course> aNewSet = (HashSet<course>) ois.readObject();
		ArrayList courses = new ArrayList();
		courses.clear();
		courses.addAll(aNewSet);
		return courses;
	}

	public static boolean checkCourseFulfillsRequirements(
			ArrayList requirements, String GEs) {
		boolean fulfills = true;
		for (int i = 0; i < requirements.size(); i++) {
			String req = (String) requirements.get(i);
			if (!GEs.contains(req)) {
				fulfills = false;
				break;
			}
		}
		return fulfills;
	}

	public static Document getCoursePage(String title) throws IOException {
		String courseTitle = title.replaceAll(" ", "%20");
		courseTitle = title.replaceAll("&", "%26");
		Document d = null;
		try {
			d = Jsoup.connect(
					"http://catalogue.uci.edu/ribbit/index.cgi?page=getcourse.rjs&code="
							+ courseTitle).get();
		} catch (java.net.SocketTimeoutException e) {
			// TimeUnit.SECONDS.sleep(10);
			d = getCoursePage(title);
		}
		return d;
	}

	public static String GEs_fulfilled(Document d) {
		Elements courseInfo = d.getElementsByTag("courseinfo");
		String s = courseInfo.text();
		int beginningIndexOfGEs = s.lastIndexOf("<p>") + 11;
		int endingIndexOfGEs = s.lastIndexOf("strong>") - 2;
		if (endingIndexOfGEs < beginningIndexOfGEs) {
			return "";
		}
		String GEs = s.substring(beginningIndexOfGEs, endingIndexOfGEs);
		String GEs2 = GEs.replaceAll("[(),.]", " ");
		return GEs2;
	}

	public static ArrayList<String> getListOfCourses(String cataloguePage)
			throws IOException {
		ArrayList<String> courseTitles = new ArrayList<String>();
		Document page = Jsoup.connect(cataloguePage).get();
		Elements courses = page.getElementsByAttribute("title");
		String courseText = courses.toString();
		int index = 1;
		int endingIndex = 0;
		while (index > 0) {
			index = courseText.indexOf("showCourse(this,", index) + 18;
			endingIndex = courseText.indexOf("')", index);
			if (endingIndex > 0) {
				String s = courseText.substring(index, endingIndex);
				String s2 = s.replace("&amp;", "&");
				courseTitles.add(s2);
				if (courseText.indexOf("showCourse(this,", endingIndex) == -1) {
					index = 0;
				} else {
					index = endingIndex;
				}
			}
		}
		return courseTitles;
	}
}	
