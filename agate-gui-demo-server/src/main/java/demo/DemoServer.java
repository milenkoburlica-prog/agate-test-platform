package demo;
import com.sun.net.httpserver.*;
import java.net.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.io.*;
public class DemoServer {
 public static void main(String[] args)throws Exception{
 int port=args.length>0?Integer.parseInt(args[0]):8767;Path root=Path.of(args.length>1?args[1]:"site").toAbsolutePath().normalize();
 HttpServer s=HttpServer.create(new InetSocketAddress("127.0.0.1",port),0);
 s.createContext("/",e->{try{String p=e.getRequestURI().getPath();byte[] body;String type;
 if(p.equals("/api/health")){body="{\"status\":\"ok\",\"version\":\"neutral-pdf-route-v2\",\"pdfRoute\":\"/demo-report.pdf\"}".getBytes();type="application/json";}
 else if(p.equals("/demo-report.pdf")&&e.getRequestMethod().equals("POST")){e.getRequestBody().readAllBytes();body=pdf();type="application/pdf";e.getResponseHeaders().set("Content-Disposition","inline; filename=learning-original.pdf");}
 else{Path f=root.resolve(p.equals("/")?"index.html":p.substring(1)).normalize();if(!f.startsWith(root)||!Files.isRegularFile(f)){System.err.println("HTTP 404: "+e.getRequestMethod()+" "+p);e.sendResponseHeaders(404,-1);return;}body=Files.readAllBytes(f);type=p.endsWith(".css")?"text/css":p.endsWith(".txt")?"text/plain":"text/html";}
 e.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");e.getResponseHeaders().set("Cache-Control","no-store");e.sendResponseHeaders(200,body.length);e.getResponseBody().write(body);
 }catch(Exception x){System.err.println("Request failed: "+x.getMessage());}finally{e.close();}});
 s.start();System.out.println("Demo version: neutral-pdf-route-v2");System.out.println("PDF route: POST /demo-report.pdf");System.out.println("AGATE learning demo: http://127.0.0.1:"+port+" (Ctrl+C to stop)");
 }
 static byte[] pdf()throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();String stream="BT /F1 18 Tf 50 750 Td (AGATE original PDF fixture) Tj ET\n";String[] objects={"<< /Type /Catalog /Pages 2 0 R >>","<< /Type /Pages /Kids [3 0 R] /Count 1 >>","<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>","<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>","<< /Length "+stream.length()+" >>\nstream\n"+stream+"endstream"};b.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));int[] offsets=new int[6];for(int i=0;i<5;i++){offsets[i+1]=b.size();b.write(((i+1)+" 0 obj\n"+objects[i]+"\nendobj\n").getBytes(StandardCharsets.US_ASCII));}int x=b.size();b.write("xref\n0 6\n0000000000 65535 f \n".getBytes());for(int i=1;i<6;i++)b.write(String.format("%010d 00000 n \n",offsets[i]).getBytes());b.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n"+x+"\n%%EOF\n").getBytes());return b.toByteArray();}
}