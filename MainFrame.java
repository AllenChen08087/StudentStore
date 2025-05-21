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
    private JScrollPane productScrollPane;
    private Map<String, Integer> productQuantities;
    private Map<String, JTextField> quantityFields;
    private JLabel totalCostLabel;
    private JButton confirmPurchaseButton;
    private JLabel timeLabel;
    private JPanel orderHistoryPanel;
    private JScrollPane orderHistoryScrollPane;
    private JPanel editProductPanel;
    private JScrollPane editProductScrollPane;

    public MainFrame() {
        store = new StudentStore();
        store.loadState();
        store.saveState();
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

        add(topPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 20));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        add(outputScrollPane, BorderLayout.CENTER);

        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(14, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] buttonLabels = {
            "Show Products",
            "Buy Product",
            "Show Sales Report (Admin Only)",
            "Show Popular Products",
            "Add New Product (Admin Only)",
            "Edit Products (Admin Only)",
            "Restock Product (Admin Only)",
            "Show Schedule",
            "Edit Employee (Admin Only)",
            "Show Employees",
            "Order List",
            "Suggestions",
            "Order History (Admin Only)",
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
                if (store.getAvailableMonths().contains(month)) {
                    captureOutput(() -> store.showSalesReport(month));
                } else {
                    outputArea.append("\n❌ No Sales Report Available for " + months[month - 1]);
                }
            });
            monthPanel.add(monthButton);
        }

        monthPanel.setVisible(false);

        orderHistoryPanel = new JPanel();
        orderHistoryPanel.setLayout(new BoxLayout(orderHistoryPanel, BoxLayout.Y_AXIS));
        orderHistoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        orderHistoryScrollPane = new JScrollPane(orderHistoryPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderHistoryScrollPane.setVisible(false);

        editProductPanel = new JPanel();
        editProductPanel.setLayout(new BoxLayout(editProductPanel, BoxLayout.Y_AXIS));
        editProductPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        editProductScrollPane = new JScrollPane(editProductPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editProductScrollPane.setVisible(false);

        JPanel leftPanel = new JPanel(new CardLayout());
        leftPanel.add(menuPanel, "menu");
        leftPanel.add(monthPanel, "months");
        leftPanel.add(orderHistoryScrollPane, "orderHistory");
        leftPanel.add(editProductScrollPane, "editProducts");
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
        quantityFields = new HashMap<>();
        productButtonPanel = new JPanel();
        productButtonPanel.setLayout(new BoxLayout(productButtonPanel, BoxLayout.Y_AXIS));
        productButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        productScrollPane = new JScrollPane(productButtonPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        productScrollPane.setVisible(false);

        totalCostLabel = new JLabel("Total: $0.00");
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 24));
        confirmPurchaseButton = new JButton("Confirm Purchase");
        confirmPurchaseButton.setFont(new Font("Arial", Font.PLAIN, 24));
        confirmPurchaseButton.addActionListener(e -> confirmPurchase());
        confirmPurchaseButton.setVisible(false);

        add(productScrollPane, BorderLayout.EAST);

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
                    showEditProductPanel();
                }
                break;
            case 7:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 61;
                }
                break;
            case 8:
                store.loadState();
                captureOutput(() -> store.displaySchedule());
                break;
            case 9:
                if (checkPassword()) {
                    showInputPanel("Choose an option:\n1. Add Employee\n2. Change Employee Availability\n3. Fire Employee\n(or click 'Go Back to Home' to cancel): ");
                    currentState = 81;
                }
                break;
            case 10:
                captureOutput(() -> store.displayEmployees());
                break;
            case 11:
                captureOutput(() -> store.displayOrderList());
                break;
            case 12:
                showInputPanel("Please enter your suggestion (or click 'Go Back to Home' to cancel): ");
                currentState = 111;
                break;
            case 13:
                if (checkPassword()) {
                    showOrderHistory();
                }
                break;
            case 14:
                outputArea.append("\nExiting... Thank you! 🛒");
                store.saveState();
                dispose();
                break;
        }
    }

    private void showProductButtons() {
        productButtonPanel.removeAll();
        productQuantities.clear();
        quantityFields.clear();
        List<Product> inventory = getInventory();

        outputArea.append("\nSelect products and quantities to buy:");
        for (Product p : inventory) {
            productQuantities.put(p.getName(), 0);

            JPanel productRow = new JPanel(new BorderLayout(10, 0));

            JLabel productLabel = new JLabel(p.getName() + " ($" + p.getSellingPrice() + ", Stock: " + p.getStock() + ")");
            productLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            JButton minusButton = new JButton("-");
            minusButton.setFont(new Font("Arial", Font.BOLD, 18));
            minusButton.setPreferredSize(new Dimension(50, 50));
            minusButton.setMargin(new Insets(0, 0, 0, 0));

            JTextField quantityField = new JTextField("0", 4);
            quantityField.setFont(new Font("Arial", Font.PLAIN, 16));
            quantityField.setHorizontalAlignment(JTextField.CENTER);
            quantityFields.put(p.getName(), quantityField);

            JButton plusButton = new JButton("+");
            plusButton.setFont(new Font("Arial", Font.BOLD, 18));
            plusButton.setPreferredSize(new Dimension(50, 50));
            plusButton.setMargin(new Insets(0, 0, 0, 0));

            minusButton.addActionListener(e -> {
                int currentQuantity = productQuantities.get(p.getName());
                if (currentQuantity > 0) {
                    productQuantities.put(p.getName(), currentQuantity - 1);
                    quantityField.setText(String.valueOf(currentQuantity - 1));
                    updateTotalCost();
                }
            });

            plusButton.addActionListener(e -> {
                int currentQuantity = productQuantities.get(p.getName());
                if (currentQuantity < p.getStock()) {
                    productQuantities.put(p.getName(), currentQuantity + 1);
                    quantityField.setText(String.valueOf(currentQuantity + 1));
                    updateTotalCost();
                }
            });

            quantityField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    validateQuantityField(p, quantityField);
                }
            });

            quantityField.addActionListener(e -> validateQuantityField(p, quantityField));

            quantityPanel.add(minusButton);
            quantityPanel.add(quantityField);
            quantityPanel.add(plusButton);

            productRow.add(productLabel, BorderLayout.CENTER);
            productRow.add(quantityPanel, BorderLayout.EAST);
            productButtonPanel.add(productRow);
        }

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        totalPanel.add(totalCostLabel);
        totalPanel.add(confirmPurchaseButton);
        productButtonPanel.add(totalPanel);

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMenuButton.setPreferredSize(new Dimension(150, 30));
        backToMenuButton.addActionListener(e -> handleGoBack());
        productButtonPanel.add(backToMenuButton);

        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
        productScrollPane.setVisible(true);
        confirmPurchaseButton.setVisible(true);
        updateTotalCost();
        revalidate();
        repaint();
    }

    private void validateQuantityField(Product product, JTextField quantityField) {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity < 0) {
                quantity = 0;
                outputArea.append("\n❌ Quantity cannot be negative for " + product.getName() + ". Set to 0.");
            } else if (quantity > product.getStock()) {
                quantity = product.getStock();
                outputArea.append("\n❌ Quantity exceeds stock for " + product.getName() + ". Set to " + quantity + ".");
            }
            productQuantities.put(product.getName(), quantity);
            quantityField.setText(String.valueOf(quantity));
        } catch (NumberFormatException e) {
            productQuantities.put(product.getName(), 0);
            quantityField.setText("0");
            outputArea.append("\n❌ Invalid quantity for " + product.getName() + ". Set to 0.");
        }
        updateTotalCost();
    }

    private void showEditProductPanel() {
        editProductPanel.removeAll();
        List<Product> inventory = getInventory();

        outputArea.append("\nSelect a product to edit:");
        for (Product p : inventory) {
            JPanel productRow = new JPanel(new BorderLayout(10, 0));
            JLabel productLabel = new JLabel(String.format("%s (Cost: $%.2f, Selling: $%.2f, Stock: %d)",
                    p.getName(), p.getCostPrice(), p.getSellingPrice(), p.getStock()));
            productLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JButton editButton = new JButton("Edit");
            editButton.setFont(new Font("Arial", Font.PLAIN, 16));
            editButton.setPreferredSize(new Dimension(100, 30));
            editButton.addActionListener(e -> showEditProductDialog(p));

            productRow.add(productLabel, BorderLayout.CENTER);
            productRow.add(editButton, BorderLayout.EAST);
            editProductPanel.add(productRow);
        }

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMenuButton.setPreferredSize(new Dimension(150, 30));
        backToMenuButton.addActionListener(e -> handleGoBack());
        editProductPanel.add(backToMenuButton);

        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        productScrollPane.setVisible(false);
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(true);
        confirmPurchaseButton.setVisible(false);
        revalidate();
        repaint();
    }

    private void showEditProductDialog(Product product) {
        JDialog dialog = new JDialog(this, "Edit Product: " + product.getName(), true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JLabel nameLabel = new JLabel("New Name:");
        JTextField nameField = new JTextField(product.getName());
        JLabel costPriceLabel = new JLabel("New Cost Price:");
        JTextField costPriceField = new JTextField(String.format("%.2f", product.getCostPrice()));
        JLabel sellingPriceLabel = new JLabel("New Selling Price:");
        JTextField sellingPriceField = new JTextField(String.format("%.2f", product.getSellingPrice()));

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                String newName = nameField.getText().trim().isEmpty() ? product.getName() : nameField.getText().trim();

                double newCostPrice = costPriceField.getText().trim().isEmpty() ?
                        product.getCostPrice() : Double.parseDouble(costPriceField.getText().trim());
                double newSellingPrice = sellingPriceField.getText().trim().isEmpty() ?
                        product.getSellingPrice() : Double.parseDouble(sellingPriceField.getText().trim());

                if (newCostPrice < 0 || newSellingPrice < 0) {
                    JOptionPane.showMessageDialog(dialog, "Prices cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                captureOutput(() -> {
                    store.editProduct(product.getName(), newName, newCostPrice, newSellingPrice);
                    store.saveState();
                });
                dialog.dispose();
                showEditProductPanel();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(costPriceLabel);
        dialog.add(costPriceField);
        dialog.add(sellingPriceLabel);
        dialog.add(sellingPriceField);
        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.add(new JLabel());
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showOrderHistory() {
        orderHistoryPanel.removeAll();
        List<Order> orders = store.getOrderHistory();

        outputArea.append("\n===== Order History =====");
        if (orders.isEmpty()) {
            outputArea.append("\nNo orders recorded.");
        } else {
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                JPanel orderRow = new JPanel(new BorderLayout(10, 0));
                JLabel orderLabel = new JLabel((i + 1) + ". " + order.toString());
                orderLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                JButton reverseButton = new JButton("Reverse");
                reverseButton.setFont(new Font("Arial", Font.PLAIN, 16));
                reverseButton.setPreferredSize(new Dimension(100, 30));
                final int orderIndex = i + 1;
                reverseButton.addActionListener(e -> {
                    captureOutput(() -> {
                        if (store.reverseOrder(orderIndex)) {
                            store.saveState();
                        }
                    });
                    showOrderHistory();
                });
                orderRow.add(orderLabel, BorderLayout.CENTER);
                orderRow.add(reverseButton, BorderLayout.EAST);
                orderRow.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                orderHistoryPanel.add(orderRow);
                orderHistoryPanel.add(Box.createVerticalStrut(10));
            }
        }

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMenuButton.setPreferredSize(new Dimension(150, 30));
        backToMenuButton.addActionListener(e -> handleGoBack());
        orderHistoryPanel.add(backToMenuButton);

        menuPanel.setVisible(false);
        monthPanel.setVisible(false);
        productScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
        orderHistoryScrollPane.setVisible(true);
        confirmPurchaseButton.setVisible(false);
        revalidate();
        repaint();
    }

    private void hideProductButtons() {
        productScrollPane.setVisible(false);
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
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
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
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
        hideInputPanel();
        monthPanel.setVisible(false);
        for (String productName : productQuantities.keySet()) {
            productQuantities.put(productName, 0);
            quantityFields.get(productName).setText("0");
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
            case 111:
                final String suggestion = input;
                captureOutput(() -> store.addSuggestion(suggestion));
                hideInputPanel();
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
            store.saveState();
            store.displaySchedule();
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
                store.saveState();
                store.displaySchedule();
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
                store.saveState();
                store.displaySchedule();
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
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
        for (String productName : productQuantities.keySet()) {
            productQuantities.put(productName, 0);
            quantityFields.get(productName).setText("0");
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
        JTextField passwordField = new JPasswordField(10);
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