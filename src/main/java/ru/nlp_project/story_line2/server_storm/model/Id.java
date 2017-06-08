package ru.nlp_project.story_line2.server_storm.model;

/**
 * Объект - идентификатор. Предназначе в первую очередь для классов объектной модели с целью
 * различной сериализации в зависимости от типа преобразования.
 * 
 * @author fedor
 *
 */
public class Id {

	private String value = "";

	public Id(String value) {
		if (value == null || value.isEmpty())
			throw new IllegalStateException("Initial id value cnnot be null/empty.");
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Id other = (Id) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}



}
