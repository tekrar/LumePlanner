package model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Cell {
		private Properties properties;
		private Geometry geometry;


		public Cell() {
			// TODO Auto-generated constructor stub
		}


		public Properties getProperties() {
			return properties;
		}


		public void setProperties(Properties properties) {
			this.properties = properties;
		}


		public Geometry getGeometry() {
			return geometry;
		}


		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}


		public Cell(Properties properties, Geometry geometry) {
			super();
			this.properties = properties;
			this.geometry = geometry;
		}




		@JsonIgnoreProperties(ignoreUnknown = true)
		public class Geometry {
			private List<List<List<Double>>> coordinates;


			public Geometry() {
				super();
			}


			public Geometry(List<List<List<Double>>> coordinates) {
				super();
				this.coordinates = coordinates;
			}


			public List<List<List<Double>>> getCoordinates() {
				return coordinates;
			}


			public void setCoordinates(List<List<List<Double>>> coordinates) {
				this.coordinates = coordinates;
			}

		}




		public class Properties {
			private String id;


			public Properties() {
				super();
			}


			public Properties(String id) {
				super();
				this.id = id;
			}


			public String getId() {
				return id;
			}


			public void setId(String id) {
				this.id = id;
			}

		}

	}
