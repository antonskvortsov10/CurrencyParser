package parser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Anton
 */

public class Parser {
    public static void main(String[] args) throws Exception {
        System.out.println("My parser of exchange rates");
        String[][] rates = getRates();
        for (int i = 0; i < rates.length; i++) {
            System.out.println(rates[i][0] + " " + rates[i][1]);
        }
        JFrame frame = new JFrame();
        frame.setTitle("Курс валют");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] colNames = {"Код валюты", "Цена"};
        JTable table = new JTable(rates, colNames);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));

        table.setFont(new Font("Serif", Font.PLAIN, 18));
        table.setRowHeight(table.getRowHeight() + 18);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);

        frame.add(scrollPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String[][] getRates() throws Exception {
        String[][] rates = null;
        HashMap<String, NodeList> result = new HashMap();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + dateFormat.format(date);
        Document doc = loadDocument(url);
        System.out.println(doc.getXmlVersion());

        NodeList nl = doc.getElementsByTagName("Valute");

        for (int i = 0; i < nl.getLength(); i++) {
            Node curr = nl.item(i);
            NodeList nlChilds = curr.getChildNodes();
            for (int j = 0; j < nlChilds.getLength(); j++) {
                if (nlChilds.item(j).getNodeName().equals("CharCode")) {
                    result.put(nlChilds.item(j).getTextContent(), nlChilds);
                }
            }
        }

        int k = 0;
        rates = new String[result.size()] [2];  // Будем хранить код соответствующей валюты и её стоимость в рублях

        for (Map.Entry<String, NodeList> entry : result.entrySet()) {
            NodeList tmp = entry.getValue();
            double value = 0;
            int nominal = 0;
            for (int i = 0; i < tmp.getLength(); i++) {
                if (tmp.item(i).getNodeName().equals("Value")) {
                    value = Double.parseDouble(tmp.item(i).getTextContent().replace(',', '.'));
                }
                else if (tmp.item(i).getNodeName().equals("Nominal")) {
                    nominal = Integer.parseInt(tmp.item(i).getTextContent());
                }
            }
            double amount = value / nominal;
            rates[k][0] = entry.getKey();
            rates[k][1] = (((double) Math.round(amount * 10000)) / 10000) + " руб.";
            k++;
        }
        return rates;
    }

    private static Document loadDocument(String url) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().parse(new URL(url).openStream());
    }
}
