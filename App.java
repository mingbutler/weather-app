import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WeatherAppGui().setVisible(true);

                // System.out.println(WeatherApp.getLocationData("Tokyo"));

                // System.out.println(WeatherApp.getCurrentTime());
            }
        });
    }
}
