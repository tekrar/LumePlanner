package services;

import io.Mongo;

import java.text.ParseException;

public class TestClass {

	public static void main(String[] args) throws ParseException {
		Mongo dao = new Mongo();
		dao.testRemoval();
		dao.testInsertion();
		dao.testQuery();
	}

}
