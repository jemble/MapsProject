package uk.ac.brookes.bourgein.mapsproject;

public interface ResultListener {
	public void onRestrieveResult(String result);
	public void onSearchResult(String result);
	public void onStoreResult(Boolean result);

}
