package model;

import org.json.JSONObject;
import org.geojson.Polygon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Cell {
		private String id;
		private double area;
		private double roads_length;
		private double max_occupancies;
		private Polygon geometry;


		public Cell() {
			// TODO Auto-generated constructor stub
		}


		public Cell(String id, double area, double roads_length, double max_occupancies, Polygon geometry) {
			super();
			this.id = id;
			this.area = area;
			this.roads_length = roads_length;
			this.max_occupancies=max_occupancies;
			this.geometry = geometry;
		}


		public String getId() {
			return id;
		}


		public void setId(String id) {
			this.id = id;
		}


		public double getArea() {
			return area;
		}


		public double getRoads_length() {
			return roads_length;
		}


		public void setRoads_length(double roads_length) {
			this.roads_length = roads_length;
		}


		public void setArea(double area) {
			this.area = area;
		}


		public double getMax_occupancies() {
			return max_occupancies;
		}


		public void setMax_occupancies(double max_occupancies) {
			this.max_occupancies = max_occupancies;
		}


		public Polygon getGeometry() {
			return geometry;
		}


		public void setGeometry(Polygon geometry) {
			this.geometry = geometry;
		}
		
		public String toJSONString() {
			JSONObject result = new JSONObject();
			
			try {
				return new ObjectMapper().writeValueAsString(this);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return result.toString();
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cell other = (Cell) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
		



	}
