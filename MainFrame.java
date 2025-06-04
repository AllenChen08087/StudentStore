import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private StudentStore store;
    private JTextArea outputArea;
    private JPanel customerPanel;
    private JPanel adminPanel;
    private JPanel mainMenuPanel;
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
    private JLabel imageLabel;
    private JPanel eastPanel;
    private CardLayout eastCardLayout;
    private JPanel blankPanel;
    private static final String IMAGE_PANEL = "image";
    private static final String PRODUCT_PANEL = "products";
    private static final String BLANK_PANEL = "blank";

    public MainFrame() {
        store = new StudentStore();
        store.loadState();
        store.saveState();
        initializeStore();

        setTitle("Student Store Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
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

        customerPanel = new JPanel();
        customerPanel.setLayout(new GridLayout(6, 1, 10, 10));
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer Options"));

        String[] customerButtonLabels = {
            "Show Products",
            "Buy Product",
            "Show Popular Products",
            "Order List",
            "Suggestions"
        };

        for (int i = 0; i < customerButtonLabels.length; i++) {
            JButton button = new JButton(customerButtonLabels[i]);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            final int option = i + 1;
            button.addActionListener(e -> handleMenuOption(option));
            customerPanel.add(button);
        }

        adminPanel = new JPanel();
        adminPanel.setLayout(new GridLayout(11, 1, 10, 10));
        adminPanel.setBorder(BorderFactory.createTitledBorder("Admin Options"));

        String[] adminButtonLabels = {
            "Show Sales Report (Admin Only)",
            "Add New Product (Admin Only)",
            "Edit Products (Admin Only)",
            "Restock Product (Admin Only)",
            "Show Schedule",
            "Edit Employee (Admin Only)",
            "Show Employees",
            "Order History (Admin Only)",
            "Export Sales Report (Admin Only)",
            "Change Password (Admin Only)",
            "Exit"
        };

        for (int i = 0; i < adminButtonLabels.length; i++) {
            JButton button = new JButton(adminButtonLabels[i]);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            final int option = i + 6;
            button.addActionListener(e -> handleMenuOption(option));
            adminPanel.add(button);
        }

        mainMenuPanel = new JPanel(new BorderLayout());
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainMenuPanel.add(customerPanel, BorderLayout.NORTH);
        mainMenuPanel.add(adminPanel, BorderLayout.CENTER);

        monthPanel = new JPanel();
        monthPanel.setLayout(new GridLayout(13, 1, 10, 10));
        monthPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };

        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.addActionListener(e -> {
            monthPanel.setVisible(false);
            mainMenuPanel.setVisible(true);
            showEastPanel(IMAGE_PANEL);
        });
        monthPanel.add(backButton);

        for (int i = 0; i < months.length; i++) {
            JButton monthButton = new JButton(months[i]);
            monthButton.setFont(new Font("Arial", Font.PLAIN, 16));
            final int month = i + 1;
            monthButton.addActionListener(e -> {
                if (store.getAvailableMonths().contains(month)) {
                    captureOutput(() -> store.showSalesReport(month));
                } else {
                    outputArea.append("\n❌ No Sales Report available for " + months[month - 1]);
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
        leftPanel.add(mainMenuPanel, "menu");
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
        productButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        productScrollPane = new JScrollPane(productButtonPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        productScrollPane.setPreferredSize(new Dimension(400, 0));

        totalCostLabel = new JLabel("Total: $0.00");
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 24));
        confirmPurchaseButton = new JButton("Confirm Purchase");
        confirmPurchaseButton.setFont(new Font("Arial", Font.PLAIN, 24));
        confirmPurchaseButton.addActionListener(e -> confirmPurchase());
        confirmPurchaseButton.setVisible(false);

        imageLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("C:\\Users\\chen6\\Downloads\\Blue and White Modern Watercolor Background Instagram Story.png");
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImage = icon.getImage().getScaledInstance(400, 550, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                imageLabel.setText("Image failed to load");
            }
        } catch (Exception e) {
            imageLabel.setText("Error loading image: " + e.getMessage());
        }

        blankPanel = new JPanel();
        blankPanel.setBackground(UIManager.getColor("Panel.background"));

        eastPanel = new JPanel();
        eastPanel.setPreferredSize(new Dimension(400, 0));
        eastCardLayout = new CardLayout();
        eastPanel.setLayout(eastCardLayout);
        eastPanel.add(imageLabel, IMAGE_PANEL);
        eastPanel.add(productScrollPane, PRODUCT_PANEL);
        eastPanel.add(blankPanel, BLANK_PANEL);
        showEastPanel(IMAGE_PANEL);

        add(eastPanel, BorderLayout.EAST);

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

    private List<Product> getInventory() {
        return store.getInventory();
    }

    private void showEastPanel(String panelName) {
        eastCardLayout.show(eastPanel, panelName);
        eastPanel.revalidate();
        eastPanel.repaint();
    }

    private void handleMenuOption(int option) {
        switch (option) {
            case 1:
                captureOutput(() -> store.displayProducts());
                showEastPanel(IMAGE_PANEL);
                break;
            case 2:
                showProductButtons();
                break;
            case 3:
                captureOutput(() -> store.showPopularAndProfitableProducts());
                showEastPanel(IMAGE_PANEL);
                break;
            case 4:
                captureOutput(() -> store.displayOrderList());
                showEastPanel(IMAGE_PANEL);
                break;
            case 5:
                showInputPanel("Please enter your suggestion (or click 'Go Back to Home' to cancel): ");
                currentState = 111;
                break;
            case 6:
                if (checkPassword()) {
                    monthPanel.setVisible(true);
                    mainMenuPanel.setVisible(false);
                    showEastPanel(BLANK_PANEL);
                }
                break;
            case 7:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 51;
                }
                break;
            case 8:
                if (checkPassword()) {
                    showEditProductPanel();
                }
                break;
            case 9:
                if (checkPassword()) {
                    showInputPanel("Enter product name (or click 'Go Back to Home' to cancel): ");
                    currentState = 61;
                }
                break;
            case 10:
                store.loadState();
                captureOutput(() -> store.displaySchedule());
                showEastPanel(IMAGE_PANEL);
                break;
            case 11:
                if (checkPassword()) {
                    showInputPanel("Choose an option:\n1. Add Employee\n2. Change Employee Availability\n3. Fire Employee\n(or click 'Go Back to Home' to cancel): ");
                    currentState = 81;
                }
                break;
            case 12:
                captureOutput(() -> store.displayEmployees());
                showEastPanel(IMAGE_PANEL);
                break;
            case 13:
                if (checkPassword()) {
                    showOrderHistory();
                }
                break;
            case 14:
                if (checkPassword()) {
                    showExportSalesReportDialog();
                }
                break;
            case 15:
                if (checkPassword()) {
                    showChangePasswordDialog();
                }
                break;
            case 16:
                outputArea.append("\nExiting... Thank you! 🛒");
                store.saveState();
                dispose();
                break;
        }
        revalidate();
        repaint();
    }

    private void captureOutput(Runnable task) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        try {
            System.setOut(ps);
            task.run();
            ps.flush();
            String output = baos.toString();
            outputArea.append("\n" + output);
        } finally {
            System.setOut(originalOut);
        }
    }

    private void showChangePasswordDialog() {
        showEastPanel(BLANK_PANEL);
        ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setVisible(true);
        showEastPanel(IMAGE_PANEL);
        if (dialog.isChanged()) {
            outputArea.append("\n✅ Password changed successfully.");
        }
    }

    private void showExportSalesReportDialog() {
        showEastPanel(BLANK_PANEL);
        JDialog dialog = new JDialog(this, "Export Sales Report", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JLabel monthLabel = new JLabel("Select Month:");
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        JComboBox<String> monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton exportButton = new JButton("Export");
        JButton cancelButton = new JButton("Cancel");

        exportButton.addActionListener(e -> {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            if (store.getAvailableMonths().contains(selectedMonth)) {
                exportSalesReport(selectedMonth, months[selectedMonth - 1]);
                dialog.dispose();
                showEastPanel(IMAGE_PANEL);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "No sales data available for " + months[selectedMonth - 1], 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            dialog.dispose();
            showEastPanel(IMAGE_PANEL);
        });

        dialog.add(monthLabel);
        dialog.add(monthComboBox);
        dialog.add(new JLabel());
        dialog.add(exportButton);
        dialog.add(new JLabel());
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void exportSalesReport(int month, String monthName) {
        String csvContent = store.generateSalesReportCSV(month);
        if (csvContent.trim().endsWith("Product Name,Units Sold,Profit,Stock Remaining")) {
            outputArea.append("\n❌ No sales data to export for " + monthName);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Sales Report for " + monthName);
        fileChooser.setSelectedFile(new File("SalesReport_" + monthName + ".csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                writer.write(csvContent);
                outputArea.append("\n✅ Sales report exported successfully to " + fileToSave.getAbsolutePath());
                outputArea.append("\nYou can now upload this CSV file to Google Sheets.");
            } catch (IOException e) {
                outputArea.append("\n❌ Error exporting sales report: " + e.getMessage());
            }
        } else {
            outputArea.append("\nExport cancelled.");
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

        JPanel productRow = new JPanel(new GridBagLayout());
        productRow.setMaximumSize(new Dimension(380, 80));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel productLabel = new JLabel(p.getName() + " ($" + p.getSellingPrice() + ", Stock: " + p.getStock() + ")");
        productLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        productLabel.setPreferredSize(new Dimension(200, 30));

        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton minusButton = new JButton("-");
        minusButton.setFont(new Font("Arial", Font.BOLD, 16));
        minusButton.setPreferredSize(new Dimension(40, 30));
        minusButton.setMargin(new Insets(0, 0, 0, 0));

        JTextField quantityField = new JTextField("0", 3);
        quantityField.setFont(new Font("Arial", Font.PLAIN, 16));
        quantityField.setHorizontalAlignment(JTextField.CENTER);
        quantityFields.put(p.getName(), quantityField);

        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("Arial", Font.BOLD, 16));
        plusButton.setPreferredSize(new Dimension(40, 30));
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        productRow.add(productLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        productRow.add(quantityPanel, gbc);

        productButtonPanel.add(productRow);
        productButtonPanel.add(Box.createVerticalStrut(10));
    }

    JPanel totalPanel = new JPanel(new GridBagLayout());
    totalPanel.setMaximumSize(new Dimension(380, 80));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    totalPanel.add(totalCostLabel, gbc);

    gbc.gridx = 1;
    totalPanel.add(confirmPurchaseButton, gbc);
    productButtonPanel.add(totalPanel);

    JButton backToMenuButton = new JButton("Back to Menu");
    backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
    backToMenuButton.setPreferredSize(new Dimension(150, 30));
    backToMenuButton.addActionListener(e -> handleGoBack());
    productButtonPanel.add(backToMenuButton);

    mainMenuPanel.setVisible(false);
    monthPanel.setVisible(false);
    orderHistoryScrollPane.setVisible(false);
    editProductScrollPane.setVisible(false);
    showEastPanel(PRODUCT_PANEL);
    confirmPurchaseButton.setVisible(true);
    System.out.println("Confirm Purchase button visibility: " + confirmPurchaseButton.isVisible());
    updateTotalCost();
    productScrollPane.revalidate();
    productScrollPane.repaint();
    eastPanel.revalidate();
    eastPanel.repaint();
    SwingUtilities.invokeLater(() -> {
        JScrollBar vertical = productScrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    });
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
            editButton.addActionListener(e -> showEditProduct(p));
            productRow.add(productLabel, BorderLayout.CENTER);
            productRow.add(editButton, BorderLayout.EAST);
            editProductPanel.add(productRow);
        }

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMenuButton.setPreferredSize(new Dimension(150, 30));
        backToMenuButton.addActionListener(e -> handleGoBack());
        editProductPanel.add(backToMenuButton);

        mainMenuPanel.setVisible(false);
        monthPanel.setVisible(false);
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(true);
        confirmPurchaseButton.setVisible(false);
        showEastPanel(BLANK_PANEL);
        revalidate();
        repaint();
    }

    private void showEditProduct(Product product) {
        showEastPanel(BLANK_PANEL);
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

        cancelButton.addActionListener(e -> {
            dialog.dispose();
            showEditProductPanel();
        });

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

        mainMenuPanel.setVisible(false);
        monthPanel.setVisible(false);
        editProductScrollPane.setVisible(false);
        orderHistoryScrollPane.setVisible(true);
        confirmPurchaseButton.setVisible(false);
        showEastPanel(BLANK_PANEL);
        revalidate();
        repaint();
    }

    private void hideProductButtons() {
        confirmPurchaseButton.setVisible(false);
        showEastPanel(BLANK_PANEL);
    }

    private boolean checkPassword() {
        PasswordDialog dialog = new PasswordDialog(this);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            outputArea.append("\n❌ Access denied or cancelled.");
            showEastPanel(IMAGE_PANEL);
            return false;
        }
        return true;
    }

    private void showInputPanel(String prompt) {
        hideProductButtons();
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
        mainMenuPanel.setVisible(false);
        monthPanel.setVisible(false);
        inputPanel.setVisible(true);
        outputArea.append("\n" + prompt);
        inputField.requestFocus();
        showEastPanel(BLANK_PANEL);
        revalidate();
        repaint();
    }

    private void hideInputPanel() {
        inputPanel.setVisible(false);
        mainMenuPanel.setVisible(true);
        currentState = 0;
        productName = "";
        tempInput = "";
        showEastPanel(IMAGE_PANEL);
        revalidate();
        repaint();
    }

    private void handleGoBack() {
        outputArea.append("\nReturning to the main menu...");
        hideProductButtons();
        orderHistoryScrollPane.setVisible(false);
        editProductScrollPane.setVisible(false);
        hideInputPanel();
        monthPanel.setVisible(false);
        for (String item : productQuantities.keySet()) {
            productQuantities.put(item, 0);
            if (quantityFields.get(item) != null) {
                quantityFields.get(item).setText("0");
            }
        }
        updateTotalCost();
        revalidate();
        repaint();
    }

    private void processInput(String input) {
        if (input.isEmpty()) return;

        input = input.strip();
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
                handleEditEmployee(input);
                break;
            case 82:
                handleAddEmployee(input);
                break;
            case 83:
                handleChangeEmployee(input);
                break;
            case 84:
                handleFireEmployee(input);
                break;
            case 85:
                handleAddEmployeeAvailability(input);
                break;
            case 86:
                handleChangeEmployeeAvailability(input);
                break;
            case 111:
                final String suggestion = input;
                captureOutput(() -> store.addSuggestion(suggestion));
                hideInputPanel();
                break;
        }
    }

    private void handleEditEmployee(String input) {
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

    private void handleChangeEmployee(String name) {
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
            if (costPrice < 0) {
                outputArea.append("\n❌ Cost price cannot be negative. Please enter a valid number.");
                outputArea.append("\nEnter cost price (or click 'Go Back to Home' to cancel): ");
                return;
            }
            tempInput = input;
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
            if (sellingPrice < 0) {
                outputArea.append("\n❌ Selling price cannot be negative. Please enter a valid number.");
                outputArea.append("\nEnter selling price (or click 'Go Back to Home' to cancel): ");
                return;
            }
            tempInput += "," + input;
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
            if (stock < 0) {
                outputArea.append("\n❌ Stock cannot be negative. Please enter a valid number.");
                outputArea.append("\nEnter stock quantity (or click 'Go Back to Home' to cancel): ");
                return;
            }
            String[] parts = tempInput.split(",");
            double costPrice = Double.parseDouble(parts[0]);
            double sellingPrice = Double.parseDouble(parts[1]);
            captureOutput(() -> {
                store.addProduct(new Product(productName, costPrice, sellingPrice, stock));
                store.saveState();
            });
            hideInputPanel();
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid quantity. Please enter a number.");
            outputArea.append("\nEnter stock quantity (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleRestockQuantity(String input) {
        try {
            int quantity = Integer.parseInt(input);
            captureOutput(() -> {
                store.restock(productName, quantity);
                store.saveState();
            });
            hideInputPanel();
        } catch (NumberFormatException e) {
            outputArea.append("\n❌ Invalid quantity. Please enter a number.");
            outputArea.append("\nEnter quantity (or click 'Go Back to Home' to cancel): ");
        }
    }

    private void handleAddEmployeeAvailability(String input) {
        try {
            String[] availabilities = input.split(",");
            if (availabilities.length != 5) {
                outputArea.append("\n❌ Please enter exactly 5 availability values (true/false) separated by commas.");
                outputArea.append("\nEnter availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
                return;
            }
            boolean[] availability = new boolean[5];
            for (int i = 0; i < 5; i++) {
                availability[i] = Boolean.parseBoolean(availabilities[i].trim());
            }
            final String name = tempInput;
            captureOutput(() -> {
                store.hireEmployee(name, availability);
                store.saveState();
                store.displaySchedule();
            });
            hideInputPanel();
        } catch (Exception e) {
            outputArea.append("\n❌ Invalid input. Please use true/false values separated by commas.");
            outputArea.append("\nEnter availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
        }
    }

    private void handleChangeEmployeeAvailability(String input) {
        try {
            String[] availabilities = input.split(",");
            if (availabilities.length != 5) {
                outputArea.append("\n❌ Please enter exactly 5 availability values (true/false) separated by commas.");
                outputArea.append("\nEnter new availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
                return;
            }
            boolean[] availability = new boolean[5];
            for (int i = 0; i < 5; i++) {
                availability[i] = Boolean.parseBoolean(availabilities[i].trim());
            }
            final String name = tempInput;
            captureOutput(() -> {
                store.changeEmployeeAvailability(name, availability);
                store.saveState();
                store.displaySchedule();
            });
            hideInputPanel();
        } catch (Exception e) {
            outputArea.append("\n❌ Invalid input. Please use true/false values separated by commas.");
            outputArea.append("\nEnter new availability for Monday to Friday (true/false) separated by commas (e.g., true,false,true,true,false): ");
        }
    }

    private void updateTotalCost() {
        double total = 0.0;
        for (Product p : getInventory()) {
            int quantity = productQuantities.getOrDefault(p.getName(), 0);
            total += quantity * p.getSellingPrice();
        }
        totalCostLabel.setText(String.format("Total: $%.2f", total));
    }

    private void confirmPurchase() {
        boolean hasItems = false;
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                hasItems = true;
                final String name = entry.getKey();
                final int quantity = entry.getValue();
                captureOutput(() -> {
                    store.sellProduct(name, quantity);
                    store.saveState();
                });
            }
        }
        if (!hasItems) {
            outputArea.append("\n❌ No items selected for purchase.");
        } else {
            outputArea.append("\n✅ Purchase completed!");
            for (String productName : productQuantities.keySet()) {
                productQuantities.put(productName, 0);
                if (quantityFields.get(productName) != null) {
                    quantityFields.get(productName).setText("0");
                }
            }
            updateTotalCost();
            hideProductButtons();
            mainMenuPanel.setVisible(true);
            showEastPanel(IMAGE_PANEL);
        }
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}

class PasswordDialog extends JDialog {
    private boolean confirmed = false;
    private JPasswordField passwordField;
    private static String currentPassword = "admin123";

    public PasswordDialog(JFrame parent) {
        super(parent, "Admin Access", true);
        setLayout(new GridLayout(3, 2, 10, 10));
        setSize(300, 150);
        setLocationRelativeTo(parent);

        JLabel label = new JLabel("Enter Password:");
        passwordField = new JPasswordField();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (password.equals(currentPassword)) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        });

        cancelButton.addActionListener(e -> dispose());

        add(label);
        add(passwordField);
        add(new JLabel());
        add(okButton);
        add(new JLabel());
        add(cancelButton);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static void setPassword(String newPassword) {
        currentPassword = newPassword;
    }

    public static String getCurrentPassword() {
        return currentPassword;
    }
}

class ChangePasswordDialog extends JDialog {
    private boolean changed = false;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePasswordDialog(JFrame parent) {
        super(parent, "Change Password", true);
        setLayout(new GridLayout(5, 2, 10, 10));
        setSize(400, 250);
        setLocationRelativeTo(parent);

        JLabel currentLabel = new JLabel("Current Password:");
        currentPasswordField = new JPasswordField();
        JLabel newLabel = new JLabel("New Password:");
        newPasswordField = new JPasswordField();
        JLabel confirmLabel = new JLabel("Confirm New Password:");
        confirmPasswordField = new JPasswordField();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!currentPassword.equals(PasswordDialog.getCurrentPassword())) {
                JOptionPane.showMessageDialog(this, "Incorrect current password.", "Error", JOptionPane.ERROR_MESSAGE);
                currentPasswordField.setText("");
                return;
            }

            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                return;
            }

            PasswordDialog.setPassword(newPassword);
            changed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        add(currentLabel);
        add(currentPasswordField);
        add(newLabel);
        add(newPasswordField);
        add(confirmLabel);
        add(confirmPasswordField);
        add(new JLabel());
        add(saveButton);
        add(new JLabel());
        add(cancelButton);
    }

    public boolean isChanged() {
        return changed;
    }
}

//431123124
