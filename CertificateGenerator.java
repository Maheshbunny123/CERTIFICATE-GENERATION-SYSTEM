import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class CertificateGenerator {
    
    private static final String CERT_DIR = "certificates/";
    private static final String DATA_DIR = "data/";
    private static final String XML_FILE = DATA_DIR + "certificates.xml";
    
    public CertificateGenerator() {
        createDirectories();
    }
    
    private void createDirectories() {
        new File(CERT_DIR).mkdirs();
        new File(DATA_DIR).mkdirs();
    }
    
    public String generateCertificate(String name, String certType, String courseName, 
                                     String date, String instructor, String hours) throws Exception {
        
        String certId = generateCertificateId();
        String filename = CERT_DIR + "certificate_" + certId + ".pdf";
        
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        
        document.open();
        
        // Add border
        PdfContentByte canvas = writer.getDirectContent();
        canvas.setLineWidth(3f);
        canvas.setColorStroke(new BaseColor(41, 128, 185));
        canvas.rectangle(30, 30, PageSize.A4.rotate().getWidth() - 60, 
                        PageSize.A4.rotate().getHeight() - 60);
        canvas.stroke();
        
        canvas.setLineWidth(1f);
        canvas.setColorStroke(new BaseColor(52, 152, 219));
        canvas.rectangle(40, 40, PageSize.A4.rotate().getWidth() - 80, 
                        PageSize.A4.rotate().getHeight() - 80);
        canvas.stroke();
        
        // Add content
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(85);
        table.setSpacingBefore(50f);
        
        // Certificate Type Header
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPaddingBottom(10);
        
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 42, Font.BOLD, 
                                   new BaseColor(41, 128, 185));
        Paragraph header = new Paragraph("CERTIFICATE", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(header);
        
        Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 20, Font.NORMAL, 
                                     new BaseColor(52, 73, 94));
        Paragraph subHeader = new Paragraph("OF " + certType.toUpperCase(), subHeaderFont);
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingBefore(5);
        headerCell.addElement(subHeader);
        
        table.addCell(headerCell);
        
        // Decorative line
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineCell.setPaddingTop(20);
        lineCell.setPaddingBottom(20);
        lineCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph line = new Paragraph("━━━━━━━━━━━━━━━━━━━━━━", 
                                      new Font(Font.FontFamily.HELVETICA, 16, Font.NORMAL, 
                                      new BaseColor(189, 195, 199)));
        line.setAlignment(Element.ALIGN_CENTER);
        lineCell.addElement(line);
        table.addCell(lineCell);
        
        // "This is to certify that"
        PdfPCell certifyCell = new PdfPCell();
        certifyCell.setBorder(Rectangle.NO_BORDER);
        certifyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        certifyCell.setPaddingTop(10);
        
        Font certifyFont = new Font(Font.FontFamily.HELVETICA, 16, Font.ITALIC, 
                                   new BaseColor(52, 73, 94));
        Paragraph certifyText = new Paragraph("This is to certify that", certifyFont);
        certifyText.setAlignment(Element.ALIGN_CENTER);
        certifyCell.addElement(certifyText);
        table.addCell(certifyCell);
        
        // Recipient Name
        PdfPCell nameCell = new PdfPCell();
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setPaddingTop(15);
        nameCell.setPaddingBottom(15);
        
        Font nameFont = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD, 
                                new BaseColor(44, 62, 80));
        Paragraph namePara = new Paragraph(name, nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        nameCell.addElement(namePara);
        
        // Underline for name
        Paragraph nameLine = new Paragraph("_________________________________", 
                                          new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, 
                                          new BaseColor(189, 195, 199)));
        nameLine.setAlignment(Element.ALIGN_CENTER);
        nameCell.addElement(nameLine);
        table.addCell(nameCell);
        
        // Description based on certificate type
        PdfPCell descCell = new PdfPCell();
        descCell.setBorder(Rectangle.NO_BORDER);
        descCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        descCell.setPaddingTop(20);
        descCell.setPaddingBottom(10);
        
        Font descFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, 
                                new BaseColor(52, 73, 94));
        String description = getDescription(certType, courseName, hours);
        Paragraph descPara = new Paragraph(description, descFont);
        descPara.setAlignment(Element.ALIGN_CENTER);
        descPara.setLeading(20);
        descCell.addElement(descPara);
        table.addCell(descCell);
        
        // Course Name (highlighted)
        if (!courseName.isEmpty()) {
            PdfPCell courseCell = new PdfPCell();
            courseCell.setBorder(Rectangle.NO_BORDER);
            courseCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            courseCell.setPaddingTop(10);
            courseCell.setPaddingBottom(20);
            
            Font courseFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, 
                                      new BaseColor(41, 128, 185));
            Paragraph coursePara = new Paragraph("\"" + courseName + "\"", courseFont);
            coursePara.setAlignment(Element.ALIGN_CENTER);
            courseCell.addElement(coursePara);
            table.addCell(courseCell);
        }
        
        // Date and Signature section
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorder(Rectangle.NO_BORDER);
        footerCell.setPaddingTop(40);
        
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        
        // Date
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, 
                                new BaseColor(52, 73, 94));
        Paragraph datePara = new Paragraph("Date: " + date, dateFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        dateCell.addElement(datePara);
        
        Paragraph dateLine = new Paragraph("_______________", 
                                          new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, 
                                          new BaseColor(189, 195, 199)));
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingBefore(5);
        dateCell.addElement(dateLine);
        
        // Instructor/Signature
        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);
        signCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph instructorPara = new Paragraph(instructor.isEmpty() ? "Authorized Signature" : instructor, 
                                                dateFont);
        instructorPara.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(instructorPara);
        
        Paragraph signLine = new Paragraph("_______________", 
                                          new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, 
                                          new BaseColor(189, 195, 199)));
        signLine.setAlignment(Element.ALIGN_CENTER);
        signLine.setSpacingBefore(5);
        signCell.addElement(signLine);
        
        Font signLabelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, 
                                     new BaseColor(127, 140, 141));
        Paragraph signLabel = new Paragraph(instructor.isEmpty() ? "Signature" : "Instructor/Authority", 
                                           signLabelFont);
        signLabel.setAlignment(Element.ALIGN_CENTER);
        signLabel.setSpacingBefore(3);
        signCell.addElement(signLabel);
        
        footerTable.addCell(dateCell);
        footerTable.addCell(signCell);
        footerCell.addElement(footerTable);
        table.addCell(footerCell);
        
        document.add(table);
        
        // Add QR Code in bottom right
        String qrData = "Certificate ID: " + certId + "\nName: " + name + "\nType: " + certType + 
                       "\nDate: " + date;
        addQRCode(writer, qrData, 720, 40);
        
        // Add Certificate ID in bottom left
        Font idFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, 
                              new BaseColor(127, 140, 141));
        Phrase idPhrase = new Phrase("Certificate ID: " + certId, idFont);
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, idPhrase, 50, 50, 0);
        
        document.close();
        
        // Log to XML
        logCertificate(certId, name, certType, courseName, date, instructor, hours);
        
        return certId;
    }
    
    private String getDescription(String certType, String courseName, String hours) {
        switch (certType.toLowerCase()) {
            case "course completion":
                return "has successfully completed the course" + 
                       (hours.isEmpty() ? "" : " with " + hours + " hours of instruction");
            case "participation":
                return "has actively participated in the program";
            case "achievement":
                return "has demonstrated exceptional achievement in";
            default:
                return "has successfully completed";
        }
    }
    
    private void addQRCode(PdfWriter writer, String data, float x, float y) throws Exception {
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, 80, 80);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        Image qrCode = Image.getInstance(baos.toByteArray());
        qrCode.setAbsolutePosition(x, y);
        qrCode.scaleAbsolute(80, 80);
        writer.getDirectContent().addImage(qrCode);
    }
    
    private String generateCertificateId() {
        return "CERT" + System.currentTimeMillis();
    }
    
    private void logCertificate(String id, String name, String type, String course, 
                               String date, String instructor, String hours) {
        try {
            File xmlFile = new File(XML_FILE);
            StringBuilder xml = new StringBuilder();
            
            if (xmlFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("</certificates>")) {
                        break;
                    }
                    xml.append(line).append("\n");
                }
                reader.close();
            } else {
                xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<certificates>\n");
            }
            
            xml.append("  <certificate>\n");
            xml.append("    <id>").append(id).append("</id>\n");
            xml.append("    <name>").append(escapeXml(name)).append("</name>\n");
            xml.append("    <type>").append(escapeXml(type)).append("</type>\n");
            xml.append("    <course>").append(escapeXml(course)).append("</course>\n");
            xml.append("    <date>").append(escapeXml(date)).append("</date>\n");
            xml.append("    <instructor>").append(escapeXml(instructor)).append("</instructor>\n");
            xml.append("    <hours>").append(escapeXml(hours)).append("</hours>\n");
            xml.append("    <generated>").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                      .format(new Date())).append("</generated>\n");
            xml.append("  </certificate>\n");
            xml.append("</certificates>");
            
            FileWriter writer = new FileWriter(xmlFile);
            writer.write(xml.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
