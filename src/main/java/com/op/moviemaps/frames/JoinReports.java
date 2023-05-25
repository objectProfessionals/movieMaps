package com.op.moviemaps.frames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.op.moviemaps.services.SharedUtils;

public class JoinReports {
	protected static String dir = "scripts/reports/";

	String[] cols = { "Backer Id", "Backer Name", "Email", "Reward Minimum",
			"Pledge Amount", "Pledged At", "Rewards Sent?", "Pledged Status",
			"Billing State/Province", "Billing Country", "Survey Response",
			"Shipping Name", "Shipping Address 1", "Shipping Address 2",
			"Shipping City", "Shipping State", "Shipping Postal Code",
			"Shipping Country Name", "Shipping Country Code",
			"Shipping Country", "Shipping Amount", "Please", "Notes" };

	public static void main(String[] args) throws IOException {
		join();
	}

	public static void join() throws IOException {
		System.out.println("Joining...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fOP = new File(src + "FullReport.csv");
		FileWriter fw = new FileWriter(fOP);

		File fDir = new File(src);
		if (fDir.isDirectory()) {
			File[] files = fDir.listFiles();
			for (File file : files) {
				if (file.getName().indexOf("Kickstarter Backer Report") == 0) {
					System.out.println(file.getName());
					BufferedReader br = new BufferedReader(new FileReader(
							file.getAbsolutePath()));
					String line;
					while ((line = br.readLine()) != null) {
						// System.out.println(line);
						fw.write(line + "\n");
					}
				}
			}
		}
		fw.close();
		System.out.println("Joined");
	}
}
