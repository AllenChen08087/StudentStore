/*
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainFrame extends JFrame {
    private StudentStore store;
    private JTextArea outputArea;
    private JPanel menuPanel;
    private JPanel monthPanel;
    private JPanel inputPanel;
    private JTextField inputField;
    private JButton submitButton;
    private JButton goBackButton;
    private int currentState = 0;
    private String productName = "";
    private String tempInput = "";
    private JPanel productButtonPanel;
    private Map<String, Integer> productQuantities;
    private Map<String, JLabel> quantityLabels;
    private JLabel totalCostLabel;
    private JButton confirmPurchaseButton;
    private JLabel timeLabel;

    public MainFrame() {
        store = new StudentStore();
        store.loadState();
        initializeStore();

        setTitle("Student Store Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                store.saveState();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        timeLabel = new JLabel("Time: " + store.getCurrentTime());
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(timeLabel, BorderLayout.EAST);
        Timer timer = new Timer(1000, e -> timeLabel.setText("Time: " + store.getCurrentTime()));
        timer.start();

        totalCostLabel = new JLabel("Total: $0.00");
        
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 16));
        confirmPurchaseButton = new JButton("Confirm Purchase");
        confirmPurchaseButton.setFont(new Font("Arial", Font.PLAIN, 16));
        confirmPurchaseButton.addActionListener(e -> confirmPurchase());
        confirmPurchaseButton.setVisible(false);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.add(totalCostLabel, BorderLayout.WEST);
        totalPanel.add(confirmPurchaseButton, BorderLayout.EAST);
        topPanel.add(totalPanel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(8, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] buttonLabels = {
            "Show Products",
            "Buy Product",
            "Show Sales Report",
            "Show Popular Products",
            "Add New Product",
            "Restock Product",
            "Show Schedule",
            "Exit"
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            JButton button = new JButton(buttonLabels[i]);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            final int option = i + 1;
            button.addActionListener(e -> handleMenuOption(option));
            menuPanel.add(button);
        }

        monthPanel = new JPanel();
        monthPanel.setLayout(new GridLayout(13, 1, 10, 10));
        monthPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };

        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 18));
        backButton.addActionListener(e -> {
            monthPanel.setVisible(false);
            menuPanel.setVisible(true);
        });
        monthPanel.add(backButton);

        for (int i = 0; i < months.length; i++) {
            JButton monthButton = new JButton(months[i]);
            monthButton.setFont(new Font("Arial", Font.PLAIN, 18));
            final int month = i + 1;
            monthButton.addActionListener(e -> {
                if (checkPassword()) {
                    if (store.getAvailableMonths().contains(month)) {
                        captureOutput(() -> store.showSalesReport(month));
                    } else {
                        outputArea.append("\n❌ No Sales Report Available for " + months[month - 1]);
                    }
                }
            });
            monthPanel.add(monthButton);
        }

        monthPanel.setVisible(false);

        JPanel leftPanel = new JPanel(new CardLayout());
        leftPanel.add(menuPanel, "menu");
        leftPanel.add(monthPanel, "months");
        add(leftPanel, BorderLayout.WEST);

        inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel inputLabel = new JLabel("Input: ");
        inputLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 16));

        goBackButton = new JButton("Go Back to Home");
        goBackButton.setFont(new Font("Arial", Font.PLAIN, 16));
        goBackButton.addActionListener(e -> handleGoBack());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(submitButton);
        buttonPanel.add(goBackButton);

        inputPanel.add(inputLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        inputPanel.setVisible(false);

        submitButton.addActionListener(e -> processInput(inputField.getText()));
        inputField.addActionListener(e -> processInput(inputField.getText()));

        add(inputPanel, BorderLayout.SOUTH);

        productQuantities = new HashMap<>();
        quantityLabels = new HashMap<>();
        productButtonPanel = new JPanel();
        productButtonPanel.setLayout(new GridLayout(0, 1, 10, 10));
        productButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(productButtonPanel, BorderLayout.EAST);
        productButtonPanel.setVisible(false);

        outputArea.setText("Welcome to the Student Store Management System!\n" +
                          "Please select an option from the menu.");

        setVisible(true);
    }

    private void initializeStore() {
        if (getInventory().isEmpty()) {
            store.hireEmployee("Alice", new boolean[]{false, true, true, true, true});
            store.hireEmployee("Bob", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Charlie", new boolean[]{true, true, true, true, true});
            store.hireEmployee("David", new boolean[]{true, true, true, true, false});
            store.hireEmployee("Eve", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Frank", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Grace", new boolean[]{false, true, true, false, false});
            store.hireEmployee("Hank", new boolean[]{false, false, true, true, true});
            store.hireEmployee("Ivy", new boolean[]{false, true, true, true, false});
            store.hireEmployee("Jack", new boolean[]{true, true, true, true, false});
            store.hireEmployee("Kara", new boolean[]{true, true, false, true, false});
            store.hireEmployee("Liam", new boolean[]{false, true, true, false, false});
            store.hireEmployee("Mia", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Noah", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Olivia", new boolean[]{true, false, false, false, false});
            store.hireEmployee("Paul", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Quinn", new boolean[]{true, true, false, true, false});
            

            store.addProduct(new Product("Sprite", 5, 25, 100));
            store.addProduct(new Product("Coke", 5, 25, 100));
            store.addProduct(new Product("Milk tea", 5, 25, 100));
            store.addProduct(new Product("Red tea", 5, 30, 100));
            store.addProduct(new Product("Chocolate milk", 5, 15, 100));

            store.createSchedule();
            store.saveState();
        }
    }

    private void handleMenuOption(int option) {
        switch (option) {
            case 1: captureOutput(() -> store.displayProducts()); break;
            case 2: showProductButtons(); break;
            case 3:
                if (checkPassword()) {
                    monthPanel.setVisible(true);
                    menuPanel.setVisible(false);
                }
                break;
            case 4: captureOutput(() -> store.showPopularAndProfitableProducts()); break;
            case 5:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 51;
                }
                break;
            case 6:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 61;
                }
                break;
            case 7:
                store.loadState();
                captureOutput(() -> store.displaySchedule());
                break;
            case 8:
                outputArea.append("\nExiting... Thank you! 🛒");
                store.saveState();
                dispose();
                break;
        }
    }

    private void showProductButtons() {
        productButtonPanel.removeAll();
        productQuantities.clear();
        quantityLabels.clear();
        List<Product> inventory = getInventory();

        outputArea.append("\nSelect products and quantities to buy:");
        for (Product p : inventory) {
            productQuantities.put(p.getName(), 0);

            JPanel productRow = new JPanel(new BorderLayout(10, 0));

            JLabel productLabel = new JLabel(p.getName() + " ($" + p.getSellingPrice() + ")");
            productLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            JButton minusButton = new JButton("-");
            minusButton.setFont(new Font("Arial", Font.BOLD, 16));
            minusButton.setBackground(new Color(147, 112, 219));
            minusButton.setForeground(Color.WHITE);
            minusButton.setPreferredSize(new Dimension(40, 40));
            minusButton.setBorder(BorderFactory.createEmptyBorder());
            minusButton.setFocusPainted(false);
            minusButton.setBorderPainted(false);
            minusButton.setOpaque(true);
            minusButton.setBorder(BorderFactory.createLineBorder(new Color(147, 112, 219), 2));
            minusButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();
                    g.setColor(b.getBackground());
                    g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    if (model.isPressed()) {
                        g.setColor(b.getBackground().darker());
                        g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    }
                    super.paint(g, c);
                }
            });

            JLabel quantityLabel = new JLabel("0");
            quantityLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            quantityLabels.put(p.getName(), quantityLabel);

            JButton plusButton = new JButton("+");
            plusButton.setFont(new Font("Arial", Font.BOLD, 16));
            plusButton.setBackground(new Color(147, 112, 219));
            plusButton.setForeground(Color.WHITE);
            plusButton.setPreferredSize(new Dimension(40, 40));
            plusButton.setBorder(BorderFactory.createEmptyBorder());
            plusButton.setFocusPainted(false);
            plusButton.setBorderPainted(false);
            plusButton.setOpaque(true);
            plusButton.setBorder(BorderFactory.createLineBorder(new Color(147, 112, 219), 2));
            plusButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();
                    g.setColor(b.getBackground());
                    g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    if (model.isPressed()) {
                        g.setColor(b.getBackground().darker());
                        g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    }
                    super.paint(g, c);
                }
            });

            minusButton.addActionListener(e -> {
                int currentQuantity = productQuantities.get(p.getName());
                if (currentQuantity > 0) {
                    productQuantities.put(p.getName(), currentQuantity - 1);
                    quantityLabels.get(p.getName()).setText(String.valueOf(currentQuantity - 1));
                    updateTotalCost();
                }
            });

            plusButton.addActionListener(e -> {
                int currentQuantity = productQuantities.get(p.getName());
                if (currentQuantity < p.getStock()) {
                    productQuantities.put(p.getName(), currentQuantity + 1);
                    quantityLabels.get(p.getName()).setText(String.valueOf(currentQuantity + 1));
                    updateTotalCost();
                }
            });

            quantityPanel.add(minusButton);
            quantityPanel.add(quantityLabel);
            quantityPanel.add(plusButton);

            productRow.add(productLabel, BorderLayout.CENTER);
            productRow.add(quantityPanel, BorderLayout.EAST);
            productButtonPanel.add(productRow);
        }

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMenuButton.addActionListener(e -> handleGoBack());
        productButtonPanel.add(backToMenuButton);

        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        productButtonPanel.setVisible(true);
        confirmPurchaseButton.setVisible(true);
        updateTotalCost();
        revalidate();
        repaint();
    }

    private void hideProductButtons() {
        productButtonPanel.setVisible(false);
        confirmPurchaseButton.setVisible(false);
    }

    private boolean checkPassword() {
        PasswordDialog dialog = new PasswordDialog(this);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            outputArea.append("\n❌ Access denied or cancelled.");
            return false;
        }
        return true;
    }

    private void showInputPanel(String prompt) {
        hideProductButtons();
        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        inputPanel.setVisible(true);
        outputArea.append("\n" + prompt);
        inputField.requestFocus();
    }

    private void hideInputPanel() {
        inputPanel.setVisible(false);
        menuPanel.setVisible(true);
        currentState = 0;
        productName = "";
        tempInput = "";
    }

    private void handleGoBack() {
        outputArea.append("\nReturning to main menu...");
        hideProductButtons();
        hideInputPanel();
        monthPanel.setVisible(false);
        for (String productName : productQuantities.keySet()) {
            productQuantities.put(productName, 0);
            quantityLabels.get(productName).setText("0");
        }
        updateTotalCost();
    }

    private void processInput(String input) {
        if (input.isEmpty()) return;

        input = input.trim();
        inputField.setText("");

        switch (currentState) {
            case 51:
                productName = input;
                outputArea.append("\nEnter cost price (or click 'Go Back to Home' to cancel): ");
                currentState = 52;
                break;
            case 52:
                handleAddCostPrice(input);
                break;
            case 53:
                handleAddSellingPrice(input);
                break;
            case 54:
                handleAddStock(input);
                break;
            case 61:
                productName = input;
                outputArea.append("\nEnter quantity (or click 'Go Back to Home' to cancel): ");
                currentState = 62;
                break;
            case 62:
                handleRestockQuantity(input);
                break;
        }
    }

    private void handleAddCostPrice(String input) {
        try {
            double costPrice = Double.parseDouble(input);
            tempInput = String.valueOf(costPrice);
            outputArea.append("\nEnter selling price (or click 'Go Back to Home' to cancel): ");
            currentState = 53;
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid price. Please enter a number.");
            outputArea.append("\nEnter cost price (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleAddSellingPrice(String input) {
        try {
            double sellingPrice = Double.parseDouble(input);
            tempInput += "," + sellingPrice;
            outputArea.append("\nEnter stock quantity (or click 'Go Back to Home' to cancel): ");
            currentState = 54;
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid price. Please enter a number.");
            outputArea.append("\nEnter selling price (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleAddStock(String input) {
        try {
            int stock = Integer.parseInt(input);
            String[] prices = tempInput.split(",");
            Product newProduct = new Product(productName, Double.parseDouble(prices[0]),
                                          Double.parseDouble(prices[1]), stock);
            store.addProduct(newProduct);
            outputArea.append("\n✅ Product '" + productName + "' added successfully!");
            hideInputPanel();
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid quantity. Please enter a number.");
            outputArea.append("\nEnter stock quantity (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleRestockQuantity(String input) {
        try {
            int quantity = Integer.parseInt(input);
            captureOutput(() -> store.restock(productName, quantity));
            hideInputPanel();
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid quantity. Please enter a number.");
            outputArea.append("\nEnter quantity (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void updateTotalCost() {
        double total = 0.0;
        List<Product> inventory = getInventory();

        for (Product p : inventory) {
            int quantity = productQuantities.get(p.getName());
            total += p.getSellingPrice() * quantity;
        }

        totalCostLabel.setText(String.format("Total: $%.2f", total));
    }

    private void confirmPurchase() {
        List<Product> inventory = getInventory();
        boolean hasItems = false;

        for (Product p : inventory) {
            int quantity = productQuantities.get(p.getName());
            if (quantity > 0) {
                hasItems = true;
                captureOutput(() -> store.sellProduct(p.getName(), quantity));
            }
        }

        if (!hasItems) {
            outputArea.append("\n❌ No products selected for purchase.");
        } else {
            outputArea.append("\n✅ Purchase completed successfully!");
        }

        hideProductButtons();
        menuPanel.setVisible(true);
        monthPanel.setVisible(false);
        for (String productName : productQuantities.keySet()) {
            productQuantities.put(productName, 0);
            quantityLabels.get(productName).setText("0");
        }
        updateTotalCost();
    }

    private void captureOutput(Runnable method) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(out));

        method.run();

        System.setOut(originalOut);
        outputArea.append("\n" + out.toString());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private List<Product> getInventory() {
        return store.getInventory();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}

class PasswordDialog extends JDialog {
    private boolean confirmed = false;

    public PasswordDialog(JFrame parent) {
        super(parent, "Enter Password", true);
        setLayout(new BorderLayout());
        setSize(300, 150);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        JTextField passwordField = new JTextField(10);
        JButton confirmButton = new JButton("Confirm");

        confirmButton.addActionListener(e -> {
            String password = passwordField.getText();
            if ("admin".equals(password)) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Password: "));
        panel.add(passwordField);
        add(panel, BorderLayout.CENTER);
        add(confirmButton, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
*/



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainFrame extends JFrame {
    private StudentStore store;
    private JTextArea outputArea;
    private JPanel menuPanel;
    private JPanel monthPanel;
    private JPanel inputPanel;
    private JTextField inputField;
    private JButton submitButton;
    private JButton goBackButton;
    private int currentState = 0;
    private String productName = "";
    private String tempInput = "";
    private JPanel productButtonPanel;
    private Map<String, Integer> productQuantities;
    private Map<String, JLabel> quantityLabels;
    private JLabel totalCostLabel;
    private JButton confirmPurchaseButton;
    private JLabel timeLabel;

    public MainFrame() {
        store = new StudentStore();
        store.loadState();
        initializeStore();

        setTitle("Student Store Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                store.saveState();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        timeLabel = new JLabel("Time: " + store.getCurrentTime());
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(timeLabel, BorderLayout.EAST);
        Timer timer = new Timer(1000, e -> timeLabel.setText("Time: " + store.getCurrentTime()));
        timer.start();

        totalCostLabel = new JLabel("Total: $0.00");
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 16));
        confirmPurchaseButton = new JButton("Confirm Purchase");
        confirmPurchaseButton.setFont(new Font("Arial", Font.PLAIN, 16));
        confirmPurchaseButton.addActionListener(e -> confirmPurchase());
        confirmPurchaseButton.setVisible(false);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.add(totalCostLabel, BorderLayout.WEST);
        totalPanel.add(confirmPurchaseButton, BorderLayout.EAST);
        topPanel.add(totalPanel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(11, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] buttonLabels = {
            "Show Products",
            "Buy Product",
            "Show Sales Report",
            "Show Popular Products",
            "Add New Product",
            "Restock Product",
            "Show Schedule",
            "Edit Employee",
            "Show Employees",
            "Exit"
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            JButton button = new JButton(buttonLabels[i]);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            final int option = i + 1;
            button.addActionListener(e -> handleMenuOption(option));
            menuPanel.add(button);
        }

        monthPanel = new JPanel();
        monthPanel.setLayout(new GridLayout(13, 1, 10, 10));
        monthPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };

        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 18));
        backButton.addActionListener(e -> {
            monthPanel.setVisible(false);
            menuPanel.setVisible(true);
        });
        monthPanel.add(backButton);

        for (int i = 0; i < months.length; i++) {
            JButton monthButton = new JButton(months[i]);
            monthButton.setFont(new Font("Arial", Font.PLAIN, 18));
            final int month = i + 1;
            monthButton.addActionListener(e -> {
                if (checkPassword()) {
                    if (store.getAvailableMonths().contains(month)) {
                        captureOutput(() -> store.showSalesReport(month));
                    } else {
                        outputArea.append("\n❌ No Sales Report Available for " + months[month - 1]);
                    }
                }
            });
            monthPanel.add(monthButton);
        }

        monthPanel.setVisible(false);

        JPanel leftPanel = new JPanel(new CardLayout());
        leftPanel.add(menuPanel, "menu");
        leftPanel.add(monthPanel, "months");
        add(leftPanel, BorderLayout.WEST);

        inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel inputLabel = new JLabel("Input: ");
        inputLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 16));

        goBackButton = new JButton("Go Back to Home");
        goBackButton.setFont(new Font("Arial", Font.PLAIN, 16));
        goBackButton.addActionListener(e -> handleGoBack());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(submitButton);
        buttonPanel.add(goBackButton);

        inputPanel.add(inputLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        inputPanel.setVisible(false);

        submitButton.addActionListener(e -> processInput(inputField.getText()));
        inputField.addActionListener(e -> processInput(inputField.getText()));

        add(inputPanel, BorderLayout.SOUTH);

        productQuantities = new HashMap<>();
        quantityLabels = new HashMap<>();
        productButtonPanel = new JPanel();
        productButtonPanel.setLayout(new GridLayout(0, 1, 10, 10));
        productButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(productButtonPanel, BorderLayout.EAST);
        productButtonPanel.setVisible(false);

        outputArea.setText("Welcome to the Student Store Management System!\n" +
                          "Please select an option from the menu.");

        setVisible(true);
    }

    private void initializeStore() {
        if (getInventory().isEmpty()) {
            store.hireEmployee("Alice", new boolean[]{false, true, true, true, true});
            store.hireEmployee("Bob", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Charlie", new boolean[]{true, true, true, true, true});
            store.hireEmployee("David", new boolean[]{true, true, true, true, false});
            store.hireEmployee("Eve", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Frank", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Grace", new boolean[]{false, true, true, false, false});
            store.hireEmployee("Hank", new boolean[]{false, false, true, true, true});
            store.hireEmployee("Ivy", new boolean[]{false, true, true, true, false});
            store.hireEmployee("Jack", new boolean[]{true, true, true, true, false});
            store.hireEmployee("Kara", new boolean[]{true, true, false, true, false});
            store.hireEmployee("Liam", new boolean[]{false, true, true, false, false});
            store.hireEmployee("Mia", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Noah", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Olivia", new boolean[]{true, false, false, false, false});
            store.hireEmployee("Paul", new boolean[]{true, true, true, true, true});
            store.hireEmployee("Quinn", new boolean[]{true, true, false, true, false});

            store.addProduct(new Product("Sprite", 5, 25, 100));
            store.addProduct(new Product("Coke", 5, 25, 100));
            store.addProduct(new Product("Milk tea", 5, 25, 100));
            store.addProduct(new Product("Red tea", 5, 30, 100));
            store.addProduct(new Product("Chocolate milk", 5, 15, 100));

            store.createSchedule();
            store.saveState();
        }
    }

    private void handleMenuOption(int option) {
        switch (option) {
            case 1: captureOutput(() -> store.displayProducts()); break;
            case 2: showProductButtons(); break;
            case 3:
                if (checkPassword()) {
                    monthPanel.setVisible(true);
                    menuPanel.setVisible(false);
                }
                break;
            case 4: captureOutput(() -> store.showPopularAndProfitableProducts()); break;
            case 5:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 51;
                }
                break;
            case 6:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 61;
                }
                break;
            case 7:
                store.loadState();
                captureOutput(() -> store.displaySchedule());
                break;
            case 8:
                if (checkPassword()) {
                    showInputPanel("Choose an option:\n1. Add Employee\n2. Change Employee Availability\n3. Fire Employee\n(or click 'Go Back to Home' to cancel): ");
                    currentState = 81;
                }
                break;
            case 9:
                captureOutput(() -> store.displayEmployees());
                break;
            case 10:
                outputArea.append("\nExiting... Thank you! 🛒");
                store.saveState();
                dispose();
                break;
        }
    }

    private void showProductButtons() {
        productButtonPanel.removeAll();
        productQuantities.clear();
        quantityLabels.clear();
        List<Product> inventory = getInventory();

        outputArea.append("\nSelect products and quantities to buy:");
        for (Product p : inventory) {
            productQuantities.put(p.getName(), 0);

            JPanel productRow = new JPanel(new BorderLayout(10, 0));

            JLabel productLabel = new JLabel(p.getName() + " ($" + p.getSellingPrice() + ")");
            productLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            JButton minusButton = new JButton("-");
            minusButton.setFont(new Font("Arial", Font.BOLD, 16));
            minusButton.setBackground(new Color(147, 112, 219));
            minusButton.setForeground(Color.WHITE);
            minusButton.setPreferredSize(new Dimension(40, 40));
            minusButton.setBorder(BorderFactory.createEmptyBorder());
            minusButton.setFocusPainted(false);
            minusButton.setBorderPainted(false);
            minusButton.setOpaque(true);
            minusButton.setBorder(BorderFactory.createLineBorder(new Color(147, 112, 219), 2));
            minusButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();
                    g.setColor(b.getBackground());
                    g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    if (model.isPressed()) {
                        g.setColor(b.getBackground().darker());
                        g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    }
                    super.paint(g, c);
                }
            });

            JLabel quantityLabel = new JLabel("0");
            quantityLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            quantityLabels.put(p.getName(), quantityLabel);

            JButton plusButton = new JButton("+");
            plusButton.setFont(new Font("Arial", Font.BOLD, 16));
            plusButton.setBackground(new Color(147, 112, 219));
            plusButton.setForeground(Color.WHITE);
            plusButton.setPreferredSize(new Dimension(40, 40));
            plusButton.setBorder(BorderFactory.createEmptyBorder());
            plusButton.setFocusPainted(false);
            plusButton.setBorderPainted(false);
            plusButton.setOpaque(true);
            plusButton.setBorder(BorderFactory.createLineBorder(new Color(147, 112, 219), 2));
            plusButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();
                    g.setColor(b.getBackground());
                    g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    if (model.isPressed()) {
                        g.setColor(b.getBackground().darker());
                        g.fillOval(0, 0, b.getWidth() - 1, b.getHeight() - 1);
                    }
                    super.paint(g, c);
                }
            });

            minusButton.addActionListener(e -> {
                int currentQuantity = productQuantities.get(p.getName());
                if (currentQuantity > 0) {
                    productQuantities.put(p.getName(), currentQuantity - 1);
                    quantityLabels.get(p.getName()).setText(String.valueOf(currentQuantity - 1));
                    updateTotalCost();
                }
            });

            plusButton.addActionListener(e -> {
                int currentQuantity = productQuantities.get(p.getName());
                if (currentQuantity < p.getStock()) {
                    productQuantities.put(p.getName(), currentQuantity + 1);
                    quantityLabels.get(p.getName()).setText(String.valueOf(currentQuantity + 1));
                    updateTotalCost();
                }
            });

            quantityPanel.add(minusButton);
            quantityPanel.add(quantityLabel);
            quantityPanel.add(plusButton);

            productRow.add(productLabel, BorderLayout.CENTER);
            productRow.add(quantityPanel, BorderLayout.EAST);
            productButtonPanel.add(productRow);
        }

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMenuButton.addActionListener(e -> handleGoBack());
        productButtonPanel.add(backToMenuButton);

        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        productButtonPanel.setVisible(true);
        confirmPurchaseButton.setVisible(true);
        updateTotalCost();
        revalidate();
        repaint();
    }

    private void hideProductButtons() {
        productButtonPanel.setVisible(false);
        confirmPurchaseButton.setVisible(false);
    }

    private boolean checkPassword() {
        PasswordDialog dialog = new PasswordDialog(this);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            outputArea.append("\n❌ Access denied or cancelled.");
            return false;
        }
        return true;
    }

    private void showInputPanel(String prompt) {
        hideProductButtons();
        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        inputPanel.setVisible(true);
        outputArea.append("\n" + prompt);
        inputField.requestFocus();
    }

    private void hideInputPanel() {
        inputPanel.setVisible(false);
        menuPanel.setVisible(true);
        currentState = 0;
        productName = "";
        tempInput = "";
    }

    private void handleGoBack() {
        outputArea.append("\nReturning to main menu...");
        hideProductButtons();
        hideInputPanel();
        monthPanel.setVisible(false);
        for (String productName : productQuantities.keySet()) {
            productQuantities.put(productName, 0);
            quantityLabels.get(productName).setText("0");
        }
        updateTotalCost();
    }

    private void processInput(String input) {
        if (input.isEmpty()) return;

        input = input.trim();
        inputField.setText("");

        switch (currentState) {
            case 51:
                productName = input;
                outputArea.append("\nEnter cost price (or click 'Go Back to Home' to cancel): ");
                currentState = 52;
                break;
            case 52:
                handleAddCostPrice(input);
                break;
            case 53:
                handleAddSellingPrice(input);
                break;
            case 54:
                handleAddStock(input);
                break;
            case 61:
                productName = input;
                outputArea.append("\nEnter quantity (or click 'Go Back to Home' to cancel): ");
                currentState = 62;
                break;
            case 62:
                handleRestockQuantity(input);
                break;
            case 81:
                handleEditEmployeeOption(input);
                break;
            case 82:
                handleAddEmployee(input);
                break;
            case 83:
                handleChangeEmployeeAvailability(input);
                break;
            case 84:
                handleFireEmployee(input);
                break;
            case 85:
                handleAddEmployeeAvailability(input);
                break;
            case 86:
                handleChangeEmployeeAvailabilityInput(input);
                break;
        }
    }

    private void handleEditEmployeeOption(String input) {
        switch (input) {
            case "1":
                outputArea.append("\nEnter employee name (or click 'Go Back to Home' to cancel): ");
                currentState = 82;
                break;
            case "2":
                outputArea.append("\nEnter employee name (or click 'Go Back to Home' to cancel): ");
                currentState = 83;
                break;
            case "3":
                outputArea.append("\nEnter employee name (or click 'Go Back to Home' to cancel): ");
                currentState = 84;
                break;
            default:
                outputArea.append("\n❌ Invalid option. Please enter 1, 2, or 3.");
                outputArea.append("\nChoose an option:\n1. Add Employee\n2. Change Employee Availability\n3. Fire Employee\n(or click 'Go Back to Home' to cancel): ");
                break;
        }
    }

    private void handleAddEmployee(String name) {
        outputArea.append("\nEnter availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
        currentState = 85;
        tempInput = name;
    }

    private void handleChangeEmployeeAvailability(String name) {
        outputArea.append("\nEnter new availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
        currentState = 86;
        tempInput = name;
    }

    private void handleFireEmployee(String name) {
        captureOutput(() -> {
            store.fireEmployee(name);
            store.saveState(); // Save the updated schedule to file
            store.displaySchedule(); // Display the updated schedule
        });
        hideInputPanel();
    }

    private void handleAddCostPrice(String input) {
        try {
            double costPrice = Double.parseDouble(input);
            tempInput = String.valueOf(costPrice);
            outputArea.append("\nEnter selling price (or click 'Go Back to Home' to cancel): ");
            currentState = 53;
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid price. Please enter a number.");
            outputArea.append("\nEnter cost price (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleAddSellingPrice(String input) {
        try {
            double sellingPrice = Double.parseDouble(input);
            tempInput += "," + sellingPrice;
            outputArea.append("\nEnter stock quantity (or click 'Go Back to Home' to cancel): ");
            currentState = 54;
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid price. Please enter a number.");
            outputArea.append("\nEnter selling price (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleAddStock(String input) {
        try {
            int stock = Integer.parseInt(input);
            String[] prices = tempInput.split(",");
            Product newProduct = new Product(productName, Double.parseDouble(prices[0]),
                                          Double.parseDouble(prices[1]), stock);
            store.addProduct(newProduct);
            outputArea.append("\n✅ Product '" + productName + "' added successfully!");
            hideInputPanel();
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid quantity. Please enter a number.");
            outputArea.append("\nEnter stock quantity (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleRestockQuantity(String input) {
        try {
            int quantity = Integer.parseInt(input);
            captureOutput(() -> store.restock(productName, quantity));
            hideInputPanel();
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid quantity. Please enter a number.");
            outputArea.append("\nEnter quantity (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleAddEmployeeAvailability(String input) {
        try {
            String[] availabilityStr = input.split(",");
            if (availabilityStr.length != 5) {
                throw new IllegalArgumentException("Invalid format. Please enter exactly 5 values.");
            }
            boolean[] availability = new boolean[5];
            for (int i = 0; i < 5; i++) {
                availability[i] = Boolean.parseBoolean(availabilityStr[i].trim());
            }
            captureOutput(() -> {
                store.hireEmployee(tempInput, availability);
                store.saveState(); // Save the updated schedule to file
                store.displaySchedule(); // Display the updated schedule
            });
            hideInputPanel();
        } catch (Exception e) {
            outputArea.append("\n❌ Invalid input. Please enter availability as true/false separated by commas.");
            outputArea.append("\nEnter availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
        }
    }

    private void handleChangeEmployeeAvailabilityInput(String input) {
        try {
            String[] availabilityStr = input.split(",");
            if (availabilityStr.length != 5) {
                throw new IllegalArgumentException("Invalid format. Please enter exactly 5 values.");
            }
            boolean[] availability = new boolean[5];
            for (int i = 0; i < 5; i++) {
                availability[i] = Boolean.parseBoolean(availabilityStr[i].trim());
            }
            captureOutput(() -> {
                store.changeEmployeeAvailability(tempInput, availability);
                store.saveState(); // Save the updated schedule to file
                store.displaySchedule(); // Display the updated schedule
            });
            hideInputPanel();
        } catch (Exception e) {
            outputArea.append("\n❌ Invalid input. Please enter availability as true/false separated by commas.");
            outputArea.append("\nEnter new availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
        }
    }

    private void updateTotalCost() {
        double total = 0.0;
        List<Product> inventory = getInventory();

        for (Product p : inventory) {
            int quantity = productQuantities.get(p.getName());
            total += p.getSellingPrice() * quantity;
        }

        totalCostLabel.setText(String.format("Total: $%.2f", total));
    }

    private void confirmPurchase() {
        List<Product> inventory = getInventory();
        boolean hasItems = false;

        for (Product p : inventory) {
            int quantity = productQuantities.get(p.getName());
            if (quantity > 0) {
                hasItems = true;
                captureOutput(() -> store.sellProduct(p.getName(), quantity));
            }
        }

        if (!hasItems) {
            outputArea.append("\n❌ No products selected for purchase.");
        } else {
            outputArea.append("\n✅ Purchase completed successfully!");
        }

        hideProductButtons();
        menuPanel.setVisible(true);
        monthPanel.setVisible(false);
        for (String productName : productQuantities.keySet()) {
            productQuantities.put(productName, 0);
            quantityLabels.get(productName).setText("0");
        }
        updateTotalCost();
    }

    private void captureOutput(Runnable method) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(out));

        method.run();

        System.setOut(originalOut);
        outputArea.append("\n" + out.toString());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private List<Product> getInventory() {
        return store.getInventory();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}

class PasswordDialog extends JDialog {
    private boolean confirmed = false;

    public PasswordDialog(JFrame parent) {
        super(parent, "Enter Password", true);
        setLayout(new BorderLayout());
        setSize(300, 150);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        JTextField passwordField = new JTextField(10);
        JButton confirmButton = new JButton("Confirm");

        confirmButton.addActionListener(e -> {
            String password = passwordField.getText();
            if ("admin".equals(password)) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Password: "));
        panel.add(passwordField);
        add(panel, BorderLayout.CENTER);
        add(confirmButton, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}