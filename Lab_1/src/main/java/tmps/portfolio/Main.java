package tmps.portfolio;

import tmps.portfolio.client.PortfolioApplication;

/**
 * Composition Root & Entry Point for Lab 1: Creational Design Patterns.
 */
public class Main {

    public static void main(String[] args) {
        try {
            PortfolioApplication app = new PortfolioApplication();
            app.runDemo();
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
