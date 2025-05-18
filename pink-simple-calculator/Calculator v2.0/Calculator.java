import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Calculator {
    private JTextField displayField;
    private JLabel historyLabel;
    private StringBuilder inputBuffer = new StringBuilder();
    private double result = 0;
    private String lastOperator = "";
    private boolean startNewInput = true;

    private final double MAX_VALUE = 9.999999999E99;
    private final double MIN_VALUE = -9.999999999E99;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculator calc = new Calculator();
            calc.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(300, 480)); // 微調視窗大小

        // 設定自訂圖示
        try {
            ImageIcon icon = new ImageIcon("calculator.png"); // 替換成您的圖示路徑
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("無法載入自訂圖示: " + e.getMessage());
        }

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.PINK);

        // 白底顯示區（含累計文字和輸入欄）
        JPanel displayFieldPanel = new JPanel();
        displayFieldPanel.setBackground(Color.WHITE);
        displayFieldPanel.setLayout(new BoxLayout(displayFieldPanel, BoxLayout.Y_AXIS));

        historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        historyLabel.setOpaque(false);
        historyLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        historyLabel.setPreferredSize(new Dimension(320, 30));
        historyLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        displayFieldPanel.add(historyLabel);

        // 黑色分隔線
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        separator.setForeground(Color.BLACK);
        displayFieldPanel.add(separator);

        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 28));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(true); // 設為可編輯，才能用監聽器處理輸入格式化
        displayField.setBackground(Color.WHITE);
        displayField.setPreferredSize(new Dimension(320, 55));
        displayField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        displayField.setBorder(BorderFactory.createEmptyBorder()); // 移除預設邊框
        displayFieldPanel.add(displayField);

        mainPanel.add(displayFieldPanel, BorderLayout.NORTH);

        // 按鈕區
        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        buttonPanel.setBackground(Color.PINK);
        String[] buttonLabels = {
                "AC", "+/-", "%", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "0", "del", ".", "="
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            button.setBackground(Color.WHITE);
            button.addActionListener(e -> handleCommand(label));
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);

        setupKeyBindings(mainPanel);
        setupBackspaceBinding();
        setupDocumentListenerForThousandSeparator();

        frame.setVisible(true);
    }

    private void setupDocumentListenerForThousandSeparator() {
        displayField.getDocument().addDocumentListener(new DocumentListener() {
            private boolean isAdjusting = false;

            @Override
            public void insertUpdate(DocumentEvent e) {
                formatText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                formatText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for plain text components
            }

            private void formatText() {
                if (isAdjusting)
                    return;

                SwingUtilities.invokeLater(() -> {
                    try {
                        isAdjusting = true;
                        String text = displayField.getText();
                        int caretPos = displayField.getCaretPosition();

                        // 去除逗號和空白
                        String pureNumber = text.replaceAll("[,\\s]", "");

                        if (pureNumber.isEmpty()) {
                            displayField.setText("");
                            return;
                        }

                        // 允許輸入帶小數點的數字
                        // 但先不格式化小數部分，避免錯誤格式化
                        String integerPart = pureNumber;
                        String decimalPart = "";
                        if (pureNumber.contains(".")) {
                            int dotIndex = pureNumber.indexOf('.');
                            integerPart = pureNumber.substring(0, dotIndex);
                            decimalPart = pureNumber.substring(dotIndex);
                        }

                        // 處理負號
                        boolean isNegative = false;
                        if (integerPart.startsWith("-")) {
                            isNegative = true;
                            integerPart = integerPart.substring(1);
                        }

                        // 格式化整數部分
                        String formattedInteger = "";
                        if (!integerPart.isEmpty()) {
                            try {
                                DecimalFormat df = new DecimalFormat("#,###");
                                formattedInteger = df.format(Long.parseLong(integerPart));
                            } catch (NumberFormatException ex) {
                                // 如果輸入的整數部分過長或不合法，直接用原字串（不格式化）
                                formattedInteger = integerPart;
                            }
                        }

                        String formatted = (isNegative ? "-" : "") + formattedInteger + decimalPart;

                        // 計算光標位置差異
                        int diff = formatted.length() - text.length();
                        int newCaretPos = caretPos + diff;
                        if (newCaretPos < 0)
                            newCaretPos = 0;
                        if (newCaretPos > formatted.length())
                            newCaretPos = formatted.length();

                        displayField.setText(formatted);
                        displayField.setCaretPosition(newCaretPos);
                    } finally {
                        isAdjusting = false;
                    }
                });
            }
        });
    }

    private void setupKeyBindings(JPanel panel) {
        for (int i = 0; i <= 9; i++) {
            addKeyBinding(panel, KeyEvent.getExtendedKeyCodeForChar('0' + i), 0, String.valueOf(i));
            addKeyBinding(panel, KeyEvent.VK_NUMPAD0 + i, 0, String.valueOf(i));
        }

        // 主鍵盤與小數字鍵盤的運算符號綁定
        addKeyBinding(panel, KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK, "+"); // Shift + = 是 +
        addKeyBinding(panel, KeyEvent.VK_MINUS, 0, "-");
        addKeyBinding(panel, KeyEvent.VK_SLASH, 0, "/");
        addKeyBinding(panel, KeyEvent.VK_8, InputEvent.SHIFT_DOWN_MASK, "*");

        addKeyBinding(panel, KeyEvent.VK_ADD, 0, "+"); // 小數字鍵盤 +
        addKeyBinding(panel, KeyEvent.VK_SUBTRACT, 0, "-"); // 小數字鍵盤 -
        addKeyBinding(panel, KeyEvent.VK_MULTIPLY, 0, "*"); // 小數字鍵盤 *
        addKeyBinding(panel, KeyEvent.VK_DIVIDE, 0, "/"); // 小數字鍵盤 /

        addKeyBinding(panel, KeyEvent.VK_PERIOD, 0, ".");
        addKeyBinding(panel, KeyEvent.VK_DECIMAL, 0, ".");
        addKeyBinding(panel, KeyEvent.VK_ENTER, 0, "=");
        addKeyBinding(panel, KeyEvent.VK_ESCAPE, 0, "AC");
        addKeyBinding(panel, KeyEvent.VK_DELETE, 0, "del");
        // 退格鍵綁定交由 setupBackspaceBinding 處理
    }

    private void setupBackspaceBinding() {
        InputMap inputMap = displayField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = displayField.getActionMap();
        KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        inputMap.put(backspace, "del");
        actionMap.put("del", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCommand("del");
            }
        });
    }

    private void addKeyBinding(JComponent component, int keyCode, int modifiers, String actionCommand) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        inputMap.put(keyStroke, actionCommand);
        actionMap.put(actionCommand, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCommand(actionCommand);
            }
        });
    }

    private void handleCommand(String command) {
        if (command.matches("[0-9]")) {
            if (startNewInput) {
                inputBuffer.setLength(0);
                startNewInput = false;
            }
            inputBuffer.append(command);
            displayField.setText(inputBuffer.toString());
        } else if (command.equals(".")) {
            if (startNewInput) {
                inputBuffer.setLength(0);
                inputBuffer.append("0.");
                startNewInput = false;
            } else if (!inputBuffer.toString().contains(".")) {
                inputBuffer.append(".");
            }
            displayField.setText(inputBuffer.toString());
        } else if (command.equals("AC")) {
            inputBuffer.setLength(0);
            displayField.setText("0");
            historyLabel.setText(" ");
            result = 0;
            lastOperator = "";
            startNewInput = true;
        } else if (command.equals("del")) {
            if (inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                displayField.setText(inputBuffer.length() == 0 ? "0" : inputBuffer.toString());
            }
        } else if (command.equals("+/-")) {
            if (inputBuffer.length() > 0) {
                if (inputBuffer.charAt(0) == '-') {
                    inputBuffer.deleteCharAt(0);
                } else {
                    inputBuffer.insert(0, '-');
                }
                displayField.setText(inputBuffer.toString());
            }
        } else if (command.equals("%")) {
            if (inputBuffer.length() > 0) {
                double value = Double.parseDouble(inputBuffer.toString()) / 100;
                if (checkOverflow(value))
                    return;
                inputBuffer = new StringBuilder(String.valueOf(value));
                displayField.setText(inputBuffer.toString());
                startNewInput = true;
            }
        } else if ("+-*/".contains(command)) {
            if (inputBuffer.length() > 0) {
                if (!startNewInput) {
                    calculate();
                    if (checkOverflow(result))
                        return;
                }
                lastOperator = command;
                historyLabel.setText(displayField.getText() + " " + command);
                startNewInput = true;
            } else if (result != 0) {
                lastOperator = command;
                historyLabel.setText(formatNumber(result) + " " + command);
                startNewInput = true;
            }
        } else if (command.equals("=")) {
            if (inputBuffer.length() > 0 && !startNewInput) {
                calculate();
                if (checkOverflow(result))
                    return;
                lastOperator = "";
                historyLabel.setText(" ");
                startNewInput = true;
            }
        }
    }

    private void calculate() {
        if (inputBuffer.length() > 0) {
            double currentValue = Double.parseDouble(inputBuffer.toString());

            switch (lastOperator) {
                case "+":
                    result += currentValue;
                    break;
                case "-":
                    result -= currentValue;
                    break;
                case "*":
                    result *= currentValue;
                    break;
                case "/":
                    if (currentValue != 0) {
                        result /= currentValue;
                    } else {
                        displayField.setText("Error");
                        historyLabel.setText(" ");
                        result = 0;
                        lastOperator = "";
                        startNewInput = true;
                        return;
                    }
                    break;
                default:
                    result = currentValue;
                    break;
            }

            String resultStr = formatNumber(result);
            displayField.setText(resultStr);
            inputBuffer = new StringBuilder(resultStr);

            if (!lastOperator.isEmpty()) {
                historyLabel.setText(resultStr + " " + lastOperator);
            }
        }
    }

    private String formatNumber(double num) {
        DecimalFormat df = new DecimalFormat("#,##0.##########");
        return df.format(num);
    }

    private boolean checkOverflow(double value) {
        if (value > MAX_VALUE || value < MIN_VALUE || Double.isInfinite(value) || Double.isNaN(value)) {
            displayField.setText("Overflow");
            historyLabel.setText(" ");
            result = 0;
            inputBuffer.setLength(0);
            lastOperator = "";
            startNewInput = true;
            return true;
        }
        return false;
    }
}
