package tpi.dgrv4.dpaa.es;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

public class ResponseHit <T> {
	private String _index;
	private String _type;
	private String _id;
	private float _score;
	private Map<String, T> _source;
	private T fields;
	private List<T> sort;
	private T _ignored;

	public String get_index() {
		return _index;
	}

	public void set_index(String _index) {
		this._index = _index;
	}

	public String get_type() {
		return _type;
	}

	public void set_type(String _type) {
		this._type = _type;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public float get_score() {
		return _score;
	}

	public void set_score(float _score) {
		this._score = _score;
	}

	public Map<String, T> get_source() {
		return _source;
	}

	public void set_source(Map<String, T> _source) {
		this._source = _source;
	}

	public T getFields() {
		return fields;
	}

	public void setFields(T fields) {
		this.fields = fields;
	}

	public List<T> getSort() {
		return sort;
	}

	public void setSort(List<T> sort) {
		this.sort = sort;
	}

	public T get_ignored() {
		return _ignored;
	}

	public void set_ignored(T _ignored) {
		this._ignored = _ignored;
	}

}