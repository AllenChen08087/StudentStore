import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Employee {
    private String name;
    private boolean[] availability;
    private int shifts;

    public Employee(String name, boolean[] availability) {
        this.name = name;
        this.availability = availability;
        this.shifts = 0;
    }

    public String getName() {
        return name;
    }

    public boolean[] getAvailability() {
        return availability;
    }

    public void setAvailability(boolean[] availability) {
        this.availability = availability;
    }

    public int getShifts() {
        return shifts;
    }

    public void setShifts(int shifts) {
        this.shifts = shifts;
    }

    public static void calculateAvailability(ArrayList<Employee> employees, ArrayList<Integer> sortedIndex, ArrayList<Integer> numAvailableShifts) {
        for (int i = 0; i < employees.size(); i++) {
            sortedIndex.add(i);
            int count = 0;
            for (boolean available : employees.get(i).getAvailability()) {
                if (available) count++;
            }
            numAvailableShifts.add(count);
        }

        for (int i = 0; i < sortedIndex.size() - 1; i++) {
            for (int j = i + 1; j < sortedIndex.size(); j++) {
                if (numAvailableShifts.get(sortedIndex.get(i)) > numAvailableShifts.get(sortedIndex.get(j))) {
                    int temp = sortedIndex.get(i);
                    sortedIndex.set(i, sortedIndex.get(j));
                    sortedIndex.set(j, temp);
                }
            }
        }
    }

    public static void setUpSchedule(ArrayList<ArrayList<String>> schedule) {
        for (int i = 0; i < 5; i++) {
            schedule.add(new ArrayList<>());
        }
    }

    public static void assignEmployeesToShifts(ArrayList<Employee> employees, ArrayList<ArrayList<String>> schedule, ArrayList<Integer> sortedIndex, ArrayList<Integer> numAvailableShifts) {
        int totalEmployees = employees.size();
        int minShifts = totalEmployees / 5;
        int extraShifts = totalEmployees % 5;
        int[] targetSlots = new int[5];
        for (int i = 0; i < 5; i++) {
            targetSlots[i] = minShifts + (i < extraShifts ? 1 : 0);
        }

        for (Employee emp : employees) {
            emp.setShifts(0);
        }

        for (int i = 0; i < 5 && totalEmployees > 0; i++) {
            for (int index : sortedIndex) {
                Employee emp = employees.get(index);
                if (emp.getAvailability()[i] && emp.getShifts() == 0 && schedule.get(i).size() < targetSlots[i]) {
                    schedule.get(i).add(emp.getName());
                    emp.setShifts(1);
                    totalEmployees--;
                    if (schedule.get(i).size() >= targetSlots[i]) {
                        break;
                    }
                }
            }
        }
    }

    public static void printSchedule(ArrayList<ArrayList<String>> schedule) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (int i = 0; i < 5; i++) {
            System.out.print(days[i] + ": ");
            System.out.println(schedule.get(i).isEmpty() ? "No one is available." : String.join(", ", schedule.get(i)));
        }
    }
}

class Product {
    private String name;
    private double costPrice;
    private double sellingPrice;
    private int stock;

    public Product(String name, double costPrice, double sellingPrice, int stock) {
        this.name = name;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getProfitMargin() {
        return sellingPrice - costPrice;
    }

    public void sellProduct(int quantity) {
        if (quantity <= stock) {
            stock -= quantity;
        } else {
            System.out.println("❌ Not enough stock for " + name);
        }
    }

    public void restockProduct(int quantity) {
        stock += quantity;
    }

    @Override
    public String toString() {
        return name + " | Price: $" + sellingPrice + " | Stock: " + stock;
    }
}

class Order {
    private String timestamp;
    private String productName;
    private int quantity;

    public Order(String timestamp, String productName, int quantity) {
        this.timestamp = timestamp;
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + productName + ": " + quantity + " unit(s)";
    }
}

class StudentStore {
    private List<Product> inventory = new ArrayList<>();
    private List<Order> orderHistory = new ArrayList<>();
    private Map<String, Integer> salesData = new HashMap<>();
    private Map<String, Double> profitData = new HashMap<>();
    private Map<Integer, Map<String, Integer>> monthlySalesData = new HashMap<>();
    private Map<Integer, Map<String, Double>> monthlyProfitData = new HashMap<>();
    private Map<Integer, Double> monthlyRevenue = new HashMap<>();
    private Map<Integer, Double> monthlyProfit = new HashMap<>();
    private Map<Integer, Map<Integer, Integer>> shiftSales = new HashMap<>();
    private double totalRevenue = 0;
    private double totalProfit = 0;
    private int lastResetMonth = -1;
    private Scanner scanner = new Scanner(System.in);

    public ArrayList<Employee> employees = new ArrayList<>();
    public ArrayList<ArrayList<String>> schedule = new ArrayList<>();

    private static final String INVENTORY_FILE = "inventory.csv";
    private static final String SALES_FILE = "sales.csv";
    private static final String MONTHLY_SALES_FILE = "monthly_sales.csv";
    private static final String SCHEDULE_FILE = "schedule.csv";
    private static final String EMPLOYEES_FILE = "employees.csv";
    private static final String SHIFT_SALES_FILE = "shift_sales.csv";
    private static final String SUGGESTIONS_TXT_FILE = "suggestions.txt";
    private static final String SUGGESTIONS_CSV_FILE = "suggestions.csv";
    private static final String ORDER_HISTORY_FILE = "order_history.csv";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId taiwanZone = ZoneId.of("Asia/Taipei");

    public String getCurrentTime() {
        LocalDateTime taiwanTime = LocalDateTime.now(taiwanZone);
        return taiwanTime.format(formatter);
    }

    public int getCurrentDayOfWeek() {
        return LocalDateTime.now(taiwanZone).getDayOfWeek().getValue() % 7;
    }

    public int getCurrentMonth() {
        return LocalDateTime.now(taiwanZone).getMonthValue();
    }

    public void loadState() {
        loadInventory();
        loadSalesData();
        loadMonthlySalesData();
        loadShiftSales();
        loadEmployees();
        loadSchedule();
        loadOrderHistory();
        lastResetMonth = getCurrentMonth();
    }

    public void saveState() {
        saveInventory();
        saveSalesData();
        saveMonthlySalesData();
        saveShiftSales();
        saveEmployees();
        saveSchedule();
        saveOrderHistory();
    }

    private void loadInventory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            inventory.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name = parts[0];
                    double costPrice = Double.parseDouble(parts[1]);
                    double sellingPrice = Double.parseDouble(parts[2]);
                    int stock = Integer.parseInt(parts[3]);
                    inventory.add(new Product(name, costPrice, sellingPrice, stock));
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, use initial state
        } catch (IOException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }

    public List<Product> getInventory() {
        return inventory;
    }

    private void saveInventory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INVENTORY_FILE))) {
            for (Product product : inventory) {
                writer.println(product.getName() + "," + product.getCostPrice() + "," +
                               product.getSellingPrice() + "," + product.getStock());
            }
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
        }
    }

    private void loadSalesData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SALES_FILE))) {
            salesData.clear();
            profitData.clear();
            totalRevenue = 0;
            totalProfit = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String productName = parts[0];
                    int unitsSold = Integer.parseInt(parts[1]);
                    double profit = Double.parseDouble(parts[2]);
                    salesData.put(productName, unitsSold);
                    profitData.put(productName, profit);
                    totalRevenue += unitsSold * getSellingPriceByName(productName);
                    totalProfit += profit;
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, use initial state
        } catch (IOException e) {
            System.out.println("Error loading sales data: " + e.getMessage());
        }
    }

    private void loadMonthlySalesData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MONTHLY_SALES_FILE))) {
            monthlySalesData.clear();
            monthlyProfitData.clear();
            monthlyRevenue.clear();
            monthlyProfit.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int month = Integer.parseInt(parts[0]);
                    String productName = parts[1];
                    int unitsSold = Integer.parseInt(parts[2]);
                    double profit = Double.parseDouble(parts[3]);
                    double revenue = Double.parseDouble(parts[4]);

                    monthlySalesData.computeIfAbsent(month, k -> new HashMap<>()).put(productName, unitsSold);
                    monthlyProfitData.computeIfAbsent(month, k -> new HashMap<>()).put(productName, profit);
                    monthlyRevenue.merge(month, revenue, Double::sum);
                    monthlyProfit.merge(month, profit, Double::sum);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, use initial state
        } catch (IOException e) {
            System.out.println("Error loading monthly sales data: " + e.getMessage());
        }
    }

    private void loadShiftSales() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SHIFT_SALES_FILE))) {
            shiftSales.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int unitsSold = Integer.parseInt(parts[2]);
                    shiftSales.computeIfAbsent(month, k -> new HashMap<>()).put(day, unitsSold);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, use initial state
        } catch (IOException e) {
            System.out.println("Error loading shift sales: " + e.getMessage());
        }
    }

    private void loadOrderHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDER_HISTORY_FILE))) {
            orderHistory.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    String timestamp = parts[0];
                    String productName = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    orderHistory.add(new Order(timestamp, productName, quantity));
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, use initial state
        } catch (IOException e) {
            System.out.println("Error loading order history: " + e.getMessage());
        }
    }

    private double getSellingPriceByName(String productName) {
        for (Product p : inventory) {
            if (p.getName().equalsIgnoreCase(productName)) {
                return p.getSellingPrice();
            }
        }
        return 0;
    }

    private double getProfitMarginByName(String productName) {
        for (Product p : inventory) {
            if (p.getName().equalsIgnoreCase(productName)) {
                return p.getProfitMargin();
            }
        }
        return 0;
    }

    private void saveSalesData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SALES_FILE))) {
            for (String productName : salesData.keySet()) {
                writer.println(productName + "," + salesData.get(productName) + "," +
                               profitData.getOrDefault(productName, 0.0));
            }
        } catch (IOException e) {
            System.out.println("Error saving sales data: " + e.getMessage());
        }
    }

    private void saveMonthlySalesData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MONTHLY_SALES_FILE))) {
            for (int month : monthlySalesData.keySet()) {
                Map<String, Integer> sales = monthlySalesData.get(month);
                Map<String, Double> profits = monthlyProfitData.get(month);
                for (String productName : sales.keySet()) {
                    writer.println(month + "," + productName + "," + sales.get(productName) + "," +
                                   profits.getOrDefault(productName, 0.0) + "," +
                                   (sales.get(productName) * getSellingPriceByName(productName)));
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving monthly sales data: " + e.getMessage());
        }
    }

    private void saveShiftSales() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SHIFT_SALES_FILE))) {
            for (int month : shiftSales.keySet()) {
                Map<Integer, Integer> sales = shiftSales.get(month);
                for (int day : sales.keySet()) {
                    writer.println(month + "," + day + "," + sales.get(day));
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving shift sales: " + e.getMessage());
        }
    }

    private void saveOrderHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ORDER_HISTORY_FILE))) {
            for (Order order : orderHistory) {
                writer.println(order.getTimestamp() + "," + order.getProductName() + "," + order.getQuantity());
            }
        } catch (IOException e) {
            System.out.println("Error saving order history: " + e.getMessage());
        }
    }

    private void loadEmployees() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEES_FILE))) {
            employees.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String name = parts[0];
                    boolean[] availability = new boolean[5];
                    for (int i = 0; i < 5; i++) {
                        availability[i] = Boolean.parseBoolean(parts[i + 1]);
                    }
                    employees.add(new Employee(name, availability));
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, use initial state
        } catch (IOException e) {
            System.out.println("Error loading employees: " + e.getMessage());
        }
    }

    private void saveEmployees() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EMPLOYEES_FILE))) {
            writer.println("Name,Monday,Tuesday,Wednesday,Thursday,Friday");
            for (Employee emp : employees) {
                StringBuilder line = new StringBuilder(emp.getName());
                for (boolean avail : emp.getAvailability()) {
                    line.append(",").append(avail);
                }
                writer.println(line.toString());
            }
        } catch (IOException e) {
            System.out.println("Error saving employees: " + e.getMessage());
        }
    }

    private void loadSchedule() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SCHEDULE_FILE))) {
            schedule.clear();
            String line;
            int day = 0;
            while ((line = reader.readLine()) != null && day < 5) {
                ArrayList<String> daySchedule = new ArrayList<>();
                if (!line.trim().isEmpty()) {
                    String[] employees = line.split(",");
                    for (String emp : employees) {
                        String trimmedEmp = emp.trim();
                        if (!trimmedEmp.isEmpty() && !daySchedule.contains(trimmedEmp)) {
                            daySchedule.add(trimmedEmp);
                        }
                    }
                }
                schedule.add(daySchedule);
                day++;
            }
            while (schedule.size() < 5) {
                schedule.add(new ArrayList<>());
            }
        } catch (FileNotFoundException e) {
            while (schedule.size() < 5) {
                schedule.add(new ArrayList<>());
            }
        } catch (IOException e) {
            System.out.println("Error loading schedule: " + e.getMessage());
        }
    }

    private void saveSchedule() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCHEDULE_FILE))) {
            writer.println("Monday,Tuesday,Wednesday,Thursday,Friday");
            for (ArrayList<String> day : schedule) {
                writer.println(day.isEmpty() ? "" : String.join(",", day));
            }
        } catch (IOException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
        }
    }

    private void checkAndResetSales() {
        int currentMonth = getCurrentMonth();
        if (lastResetMonth != -1 && currentMonth != lastResetMonth) {
            monthlySalesData.put(lastResetMonth, new HashMap<>(salesData));
            monthlyProfitData.put(lastResetMonth, new HashMap<>(profitData));
            monthlyRevenue.put(lastResetMonth, totalRevenue);
            monthlyProfit.put(lastResetMonth, totalProfit);

            salesData.clear();
            profitData.clear();
            totalRevenue = 0;
            totalProfit = 0;
            lastResetMonth = currentMonth;
            System.out.println("✅ Sales data automatically reset for new month: " + currentMonth);
        }
    }

    public void displaySchedule() {
        System.out.println("Employee Schedule:");
        if (schedule.isEmpty() || schedule.size() < 5) {
            System.out.println("No schedule has been created yet.");
        } else {
            Employee.printSchedule(schedule);
        }
    }

    public void hireEmployee(String name, boolean[] availability) {
        employees.add(new Employee(name, availability));
        createSchedule();
        System.out.println("✅ Employee '" + name + "' added successfully!");
    }

    public void fireEmployee(String name) {
        boolean flag = false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getName().equals(name)) {
                employees.remove(i);
                System.out.println(name + " has been fired.");
                createSchedule();
                flag = true;
                break;
            }
        }
        if (!flag) {
            System.out.println(name + " isn't an employee");
        }
    }

    public void changeEmployeeAvailability(String name, boolean[] newAvailability) {
        for (Employee emp : employees) {
            if (emp.getName().equals(name)) {
                emp.setAvailability(newAvailability);
                createSchedule();
                System.out.println("Availability updated for " + name);
                return;
            }
        }
        System.out.println(name + " isn't an employee");
    }

    public void createSchedule() {
        schedule.clear();
        ArrayList<Integer> sortedIndex = new ArrayList<>();
        ArrayList<Integer> numAvailableShifts = new ArrayList<>();

        Employee.calculateAvailability(employees, sortedIndex, numAvailableShifts);
        Employee.setUpSchedule(schedule);
        Employee.assignEmployeesToShifts(employees, schedule, sortedIndex, numAvailableShifts);
    }

    public void addProduct(Product product) {
        inventory.add(product);
    }

    public void displayProducts() {
        checkAndResetSales();
        System.out.println("\n===== Available Products =====");
        if (inventory.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        for (Product product : inventory) {
            System.out.println(product);
        }
    }

    public void sellProduct(String productName, int quantity) {
        checkAndResetSales();
        for (Product product : inventory) {
            if (product.getName().equalsIgnoreCase(productName)) {
                if (quantity > 0 && quantity <= product.getStock()) {
                    product.sellProduct(quantity);
                    double revenue = product.getSellingPrice() * quantity;
                    double profit = product.getProfitMargin() * quantity;

                    salesData.put(productName, salesData.getOrDefault(productName, 0) + quantity);
                    profitData.put(productName, profitData.getOrDefault(productName, 0.0) + profit);
                    totalRevenue += revenue;
                    totalProfit += profit;

                    int currentMonth = getCurrentMonth();
                    Map<String, Integer> monthSales = monthlySalesData.computeIfAbsent(currentMonth, k -> new HashMap<>());
                    Map<String, Double> monthProfits = monthlyProfitData.computeIfAbsent(currentMonth, k -> new HashMap<>());
                    monthSales.put(productName, monthSales.getOrDefault(productName, 0) + quantity);
                    monthProfits.put(productName, monthProfits.getOrDefault(productName, 0.0) + profit);
                    monthlyRevenue.merge(currentMonth, revenue, Double::sum);
                    monthlyProfit.merge(currentMonth, profit, Double::sum);

                    int currentDay = getCurrentDayOfWeek();
                    shiftSales.computeIfAbsent(currentMonth, k -> new HashMap<>())
                             .merge(currentDay, quantity, Integer::sum);

                    orderHistory.add(new Order(getCurrentTime(), productName, quantity));

                    System.out.println("\n✅ Sold " + quantity + "x " + productName + " successfully!");
                } else {
                    System.out.println("\n❌ Not enough stock or invalid quantity.");
                }
                return;
            }
        }
        System.out.println("\n❌ Product not found.");
    }

    public void showSalesReport(int month) {
        checkAndResetSales();
        System.out.println("\n===== Sales Report for Month " + month + " =====");
        Map<String, Integer> sales = monthlySalesData.getOrDefault(month, new HashMap<>());
        Map<String, Double> profits = monthlyProfitData.getOrDefault(month, new HashMap<>());
        Double revenue = monthlyRevenue.getOrDefault(month, 0.0);
        Double profit = monthlyProfit.getOrDefault(month, 0.0);

        if (sales.isEmpty()) {
            System.out.println("No sales data available for this month.");
            return;
        }

        for (Map.Entry<String, Integer> entry : sales.entrySet()) {
            String productName = entry.getKey();
            int unitsSold = entry.getValue();
            double productProfit = profits.getOrDefault(productName, 0.0);
            int remainingStock = 0;
            for (Product p : inventory) {
                if (p.getName().equalsIgnoreCase(productName)) {
                    remainingStock = p.getStock();
                    break;
                }
            }
            System.out.printf("%s: %d units sold, Profit: $%.2f, Stock Remaining: %d%n",
                    productName, unitsSold, productProfit, remainingStock);
        }
        System.out.printf("Total Revenue: $%.2f%n", revenue);
        System.out.printf("Total Profit: $%.2f%n", profit);

        Map<Integer, Integer> monthlyShiftSales = shiftSales.getOrDefault(month, new HashMap<>());
        int maxSalesDay = -1;
        int maxSales = 0;
        for (Map.Entry<Integer, Integer> entry : monthlyShiftSales.entrySet()) {
            if (entry.getValue() > maxSales) {
                maxSales = entry.getValue();
                maxSalesDay = entry.getKey();
            }
        }
        if (maxSalesDay != -1 && maxSales > 0) {
            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            System.out.println("Most Popular Day: " + days[maxSalesDay]);
            System.out.print("Most Popular Sellers: ");
            ArrayList<String> dayEmployees = schedule.get(maxSalesDay % 5);
            if (dayEmployees.isEmpty()) {
                System.out.println("No employees scheduled.");
            } else {
                System.out.println(String.join(", ", dayEmployees));
            }
        }
    }

    public String generateSalesReportCSV(int month) {
        checkAndResetSales();
        StringBuilder csvContent = new StringBuilder();
        Map<String, Integer> sales = monthlySalesData.getOrDefault(month, new HashMap<>());
        Map<String, Double> profits = monthlyProfitData.getOrDefault(month, new HashMap<>());
        Double revenue = monthlyRevenue.getOrDefault(month, 0.0);
        Double profit = monthlyProfit.getOrDefault(month, 0.0);

        // Header
        csvContent.append("Product Name,Units Sold,Profit,Stock Remaining\n");

        if (sales.isEmpty()) {
            return csvContent.toString();
        }

        // Product rows
        for (Map.Entry<String, Integer> entry : sales.entrySet()) {
            String productName = entry.getKey();
            int unitsSold = entry.getValue();
            double productProfit = profits.getOrDefault(productName, 0.0);
            int remainingStock = 0;
            for (Product p : inventory) {
                if (p.getName().equalsIgnoreCase(productName)) {
                    remainingStock = p.getStock();
                    break;
                }
            }
            csvContent.append(String.format("\"%s\",%d,%.2f,%d\n", 
                productName, unitsSold, productProfit, remainingStock));
        }

        // Total rows
        csvContent.append(String.format("Total Revenue,,%.2f,\n", revenue));
        csvContent.append(String.format("Total Profit,,%.2f,\n", profit));

        return csvContent.toString();
    }

    public void displayOrderHistory() {
        checkAndResetSales();
        System.out.println("\n===== Order History =====");
        if (orderHistory.isEmpty()) {
            System.out.println("No orders recorded.");
            return;
        }
        for (int i = 0; i < orderHistory.size(); i++) {
            System.out.println((i + 1) + ". " + orderHistory.get(i));
        }
    }

    public boolean reverseOrder(int orderIndex) {
        checkAndResetSales();
        if (orderIndex < 1 || orderIndex > orderHistory.size()) {
            System.out.println("\n❌ Invalid order number.");
            return false;
        }

        Order order = orderHistory.get(orderIndex - 1);
        String productName = order.getProductName();
        int quantity = order.getQuantity();
        String timestamp = order.getTimestamp();

        // Restock the product
        for (Product product : inventory) {
            if (product.getName().equalsIgnoreCase(productName)) {
                product.restockProduct(quantity);
                break;
            }
        }

        // Update sales data
        salesData.put(productName, salesData.getOrDefault(productName, 0) - quantity);
        if (salesData.get(productName) <= 0) {
            salesData.remove(productName);
            profitData.remove(productName);
        } else {
            double profit = getProfitMarginByName(productName) * salesData.get(productName);
            profitData.put(productName, profit);
        }

        // Update total revenue and profit
        double revenue = getSellingPriceByName(productName) * quantity;
        double profit = getProfitMarginByName(productName) * quantity;
        totalRevenue -= revenue;
        totalProfit -= profit;

        // Update monthly sales data
        int month = LocalDateTime.parse(timestamp, formatter).getMonthValue();
        Map<String, Integer> monthSales = monthlySalesData.computeIfAbsent(month, k -> new HashMap<>());
        Map<String, Double> monthProfits = monthlyProfitData.computeIfAbsent(month, k -> new HashMap<>());
        monthSales.put(productName, monthSales.getOrDefault(productName, 0) - quantity);
        if (monthSales.get(productName) <= 0) {
            monthSales.remove(productName);
            monthProfits.remove(productName);
        } else {
            monthProfits.put(productName, monthProfits.getOrDefault(productName, 0.0) - profit);
        }
        monthlyRevenue.merge(month, -revenue, Double::sum);
        monthlyProfit.merge(month, -profit, Double::sum);

        // Update shift sales
        int day = LocalDateTime.parse(timestamp, formatter).getDayOfWeek().getValue() % 7;
        Map<Integer, Integer> monthShiftSales = shiftSales.computeIfAbsent(month, k -> new HashMap<>());
        monthShiftSales.merge(day, -quantity, Integer::sum);
        if (monthShiftSales.get(day) <= 0) {
            monthShiftSales.remove(day);
        }

        // Remove the order
        orderHistory.remove(orderIndex - 1);
        System.out.println("\n✅ Order reversed: " + quantity + "x " + productName + " at " + timestamp);
        return true;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public Set<Integer> getAvailableMonths() {
        checkAndResetSales();
        return monthlySalesData.keySet();
    }

    public void showPopularAndProfitableProducts(int month) {
        checkAndResetSales();
        if (inventory.isEmpty()) {
            System.out.println("\nNo products in inventory.");
            return;
        }

        Map<String, Integer> sales = monthlySalesData.getOrDefault(month, new HashMap<>());
        Map<String, Double> profits = monthlyProfitData.getOrDefault(month, new HashMap<>());

        if (sales.isEmpty() && inventory.stream().allMatch(p -> sales.getOrDefault(p.getName(), 0) == 0)) {
            System.out.println("\nNo sales data available for month " + month + ".");
            return;
        }

        List<String> mostPopular = new ArrayList<>();
        List<String> leastPopular = new ArrayList<>();
        List<String> mostProfitable = new ArrayList<>();
        int maxSales = 0;
        int minSales = Integer.MAX_VALUE;
        double maxProfit = 0.0;

        // First pass: determine max/min sales and max profit
        for (Product product : inventory) {
            String productName = product.getName();
            int salesCount = sales.getOrDefault(productName, 0);
            double productProfit = profits.getOrDefault(productName, 0.0);

            if (salesCount > maxSales) {
                maxSales = salesCount;
            }
            if (salesCount < minSales) {
                minSales = salesCount;
            }
            if (productProfit > maxProfit) {
                maxProfit = productProfit;
            }
        }

        // Second pass: collect all products matching max/min sales and max profit
        for (Product product : inventory) {
            String productName = product.getName();
            int salesCount = sales.getOrDefault(productName, 0);
            double productProfit = profits.getOrDefault(productName, 0.0);

            if (salesCount == maxSales && salesCount > 0) {
                mostPopular.add(productName);
            }
            if (salesCount == minSales) {
                leastPopular.add(productName);
            }
            if (productProfit == maxProfit && productProfit > 0) {
                mostProfitable.add(productName);
            }
        }

        System.out.println("\n===== Product Insights for Month " + month + " =====");
        if (!mostPopular.isEmpty()) {
            System.out.println("🔥 Most Popular Product(s): " + String.join(", ", mostPopular) + " (" + maxSales + " units sold)");
        } else {
            System.out.println("🔥 Most Popular Product(s): None");
        }
        if (!leastPopular.isEmpty()) {
            System.out.println("🧊 Least Popular Product(s): " + String.join(", ", leastPopular) + " (" + minSales + " units sold)");
        } else {
            System.out.println("🧊 Least Popular Product(s): None");
        }
        
    }

    public void showPopularAndProfitableProducts() {
        showPopularAndProfitableProducts(getCurrentMonth());
    }

    public void editProduct(String oldName, String newName, double newCostPrice, double newSellingPrice) {
        checkAndResetSales();
        for (Product product : inventory) {
            if (product.getName().equalsIgnoreCase(oldName)) {
                // Update inventory
                product.setName(newName);
                product.setCostPrice(newCostPrice);
                product.setSellingPrice(newSellingPrice);

                // Update sales data
                if (salesData.containsKey(oldName)) {
                    int unitsSold = salesData.remove(oldName);
                    double newProfit = (newSellingPrice - newCostPrice) * unitsSold;
                    salesData.put(newName, unitsSold);
                    profitData.remove(oldName);
                    profitData.put(newName, newProfit);
                    totalProfit = profitData.values().stream().mapToDouble(Double::doubleValue).sum();
                    totalRevenue = salesData.entrySet().stream()
                            .mapToDouble(e -> e.getValue() * getSellingPriceByName(e.getKey()))
                            .sum();
                }

                // Update monthly sales data
                for (Map<String, Integer> monthSales : monthlySalesData.values()) {
                    if (monthSales.containsKey(oldName)) {
                        int unitsSold = monthSales.remove(oldName);
                        monthSales.put(newName, unitsSold);
                    }
                }
                for (Map<String, Double> monthProfits : monthlyProfitData.values()) {
                    if (monthProfits.containsKey(oldName)) {
                        int unitsSold = monthlySalesData.get(getCurrentMonth()).getOrDefault(newName, 0);
                        double newProfit = (newSellingPrice - newCostPrice) * unitsSold;
                        monthProfits.remove(oldName);
                        monthProfits.put(newName, newProfit);
                    }
                }
                for (int month : monthlyRevenue.keySet()) {
                    double revenue = monthlySalesData.getOrDefault(month, new HashMap<>())
                            .entrySet().stream()
                            .mapToDouble(e -> e.getValue() * getSellingPriceByName(e.getKey()))
                            .sum();
                    monthlyRevenue.put(month, revenue);
                }
                for (int month : monthlyProfit.keySet()) {
                    double profit = monthlyProfitData.getOrDefault(month, new HashMap<>())
                            .values().stream()
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    monthlyProfit.put(month, profit);
                }

                // Update order history
                for (Order order : orderHistory) {
                    if (order.getProductName().equalsIgnoreCase(oldName)) {
                        order.setProductName(newName);
                    }
                }

                System.out.println("\n✅ Product '" + oldName + "' updated to '" + newName + "' successfully!");
                return;
            }
        }
        System.out.println("\n❌ Product '" + oldName + "' not found.");
    }

    public void addNewProduct() {
        checkAndResetSales();
        System.out.println("\n===== Add New Product =====");

        System.out.print("Enter product name: ");
        String name = scanner.nextLine();

        double costPrice = getValidDoubleInput("Enter cost price: ");
        double sellingPrice = getValidDoubleInput("Enter selling price: ");
        int stock = getValidIntInput("Enter stock quantity: ");

        Product newProduct = new Product(name, costPrice, sellingPrice, stock);
        inventory.add(newProduct);
        System.out.println("\n✅ Product '" + name + "' added successfully!");
    }

    public void restock(String productName, int quantity) {
        checkAndResetSales();
        if (quantity <= 0) {
            System.out.println("\n❌ Invalid quantity. Please enter a positive number.");
            return;
        }

        for (Product product : inventory) {
            if (product.getName().equalsIgnoreCase(productName)) {
                product.setStock(product.getStock() + quantity);
                System.out.println("\n✅ " + quantity + " units added to " + productName + ". New stock: " + product.getStock());
                return;
            }
        }
        System.out.println("\n❌ Product not found.");
    }

    private double getValidDoubleInput(String message) {
        double value;
        while (true) {
            System.out.print(message);
            if (scanner.hasNextDouble()) {
                value = scanner.nextDouble();
                if (value >= 0) {
                    scanner.nextLine();
                    return value;
                }
            } else {
                scanner.next();
            }
            System.out.println("\n❌ Invalid input. Please enter a valid non-negative number.");
        }
    }

    private int getValidIntInput(String message) {
        int value;
        while (true) {
            System.out.print(message);
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value >= 0) {
                    scanner.nextLine();
                    return value;
                }
            } else {
                scanner.next();
            }
            System.out.println("\n❌ Invalid input. Please enter a valid non-negative integer.");
        }
    }

    public void displayEmployees() {
        System.out.println("\n===== Employee List =====");
        if (employees.isEmpty()) {
            System.out.println("No employees available.");
            return;
        }
        for (Employee emp : employees) {
            System.out.println("Name: " + emp.getName());
            System.out.print("Availability: ");
            boolean[] availability = emp.getAvailability();
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
            for (int i = 0; i < availability.length; i++) {
                System.out.print(days[i] + ": " + (availability[i] ? "Available" : "Not Available") + ", ");
            }
            System.out.println();
        }
    }

    public void displayOrderList() {
        System.out.println("\n===== Order List (Products with Stock ≤ 10) =====");
        boolean hasLowStock = false;
        for (Product product : inventory) {
            if (product.getStock() <= 10) {
                System.out.println(product.getName() + " | Stock: " + product.getStock());
                hasLowStock = true;
            }
        }
        if (!hasLowStock) {
            System.out.println("No products need restocking (all stock > 10).");
        }
    }

    public void addSuggestion(String suggestion) {
        String timestamp = getCurrentTime();
        String formattedSuggestion = "[" + timestamp + "] " + suggestion;

        try (PrintWriter writer = new PrintWriter(new FileWriter(SUGGESTIONS_TXT_FILE, true))) {
            writer.println(formattedSuggestion);
        } catch (IOException e) {
            System.out.println("Error saving suggestion to TXT file: " + e.getMessage());
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(SUGGESTIONS_CSV_FILE, true))) {
            File file = new File(SUGGESTIONS_CSV_FILE);
            if (file.length() == 0) {
                writer.println("Timestamp,Suggestion");
            }
            String escapedSuggestion = "\"" + suggestion.replace("\"", "\"\"") + "\"";
            writer.println(timestamp + "," + escapedSuggestion);
        } catch (IOException e) {
            System.out.println("Error saving suggestion to CSV file: " + e.getMessage());
            return;
        }

        System.out.println("\n✅ Suggestion added successfully!");
    }
}