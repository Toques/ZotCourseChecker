package toques;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import toques.view.CourseOverviewController;
import toques.view.course;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader; 
public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	private CourseOverviewController controller;
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("ZotChecker");
		initRootLayout();
		showCourseOverview();
		primaryStage.setResizable(false);

	}
	public void initRootLayout()
	{
		
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/rootLayout.fxml"));
			try {
				rootLayout = (BorderPane) loader.load();
				Scene scene = new Scene(rootLayout);
				primaryStage.setScene(scene);
				primaryStage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
	public void showCourseOverview()
	{
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/CourseOverview.fxml"));
			AnchorPane courseOverview = (AnchorPane) loader.load();
			
			rootLayout.setCenter(courseOverview);
			controller = loader.getController();
			controller.setMainApp(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		public Stage getPrimaryStage()
		{
			return primaryStage;
		}

	public static void main(String[] args) {
		launch(args);
	}
	
	public ObservableList<String> giveCourses(final CheckBox two, final CheckBox three, final CheckBox four, final CheckBox five, final CheckBox six, final CheckBox seven, final CheckBox eight) throws InterruptedException, ExecutionException
	{
	Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
		
		@Override
		protected ObservableList<String> call() throws Exception{
		
		
		ArrayList<String> reqs = getWantedRequirements(two,three,four,five,six,seven,eight);
		ArrayList<String> courses = getListOfCourses("http://catalogue.uci.edu/informationforadmittedstudents/requirementsforabachelorsdegree/");
		String filename = "savedHashSetCourses.dat";
		Path courseFile = Paths.get(
				filename);
		boolean haveFile = Files.exists(courseFile);
		ArrayList<String> goodCourses = new ArrayList<String>();
		if (haveFile) {
			
			goodCourses = giveGoodCourses(courseFile, reqs, courses);
		} else {
			
			goodCourses = giveGoodCourses(reqs, courses, courseFile);
		}
	
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(goodCourses);
		goodCourses.clear();
		goodCourses.addAll(hs);
		Collections.sort(goodCourses);
		ObservableList<String> sortedCourses = FXCollections
				.observableArrayList();
		sortedCourses.addAll(goodCourses);
		
		return sortedCourses;
		}
		};
		
		Thread th  = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		//task.run();
		return task.get();

}
	public ArrayList<String> getWantedRequirements(CheckBox two, CheckBox three, CheckBox four, CheckBox five, CheckBox six, CheckBox seven, CheckBox eight) {
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
