public interface Callback {
    void onResponse(String response);
    void onFailure(IOException e);
}