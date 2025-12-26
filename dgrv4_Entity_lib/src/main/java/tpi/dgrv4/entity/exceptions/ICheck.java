package tpi.dgrv4.entity.exceptions;


public interface ICheck {
	
	
	public abstract DgrRtnCode getRtnCode();

	public abstract String getMessage(String locale);

}
