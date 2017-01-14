package com.gbbtbb.homehub;

// reused from this: https://android.googlesource.com/platform/tools/motodev/+/b613ddc1cf740750ab9dc20f1b051a2f04f5dbef/src/plugins/android/src/com/motorola/studio/android/utilities/TelnetFrameworkAndroid.java

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class CustomTelnetClient
{
    private TelnetClient telnetClient;
    private long timeout = 20000L;

    public synchronized void connect(String telnetHost, int telnetPort) throws IOException
    {
        if ((telnetClient == null) || ((telnetClient != null) && (!telnetClient.isConnected())))
        {
            telnetClient = new TelnetClient(telnetHost);
            telnetClient.connect(telnetHost, telnetPort);
        }
    }

    public synchronized void disconnect() throws IOException
    {
        if ((telnetClient != null) && (telnetClient.isConnected()))
        {
            telnetClient.disconnect();
        }
    }

    public synchronized String write(String telnetInputText, String[] waitFor) throws IOException
    {
        PrintWriter commandWriter = null;
        try
        {
            commandWriter = new PrintWriter(telnetClient.getOutputStream());
            commandWriter.println(telnetInputText);
            commandWriter.flush();
            if (waitFor != null)
            {
                return waitFor(waitFor);
            }
        }
        finally
        {
            if (commandWriter != null)
            {
                commandWriter.close();
            }
        }
        return null;
    }

    public boolean isConnected()
    {
        boolean connected = false;
        if (telnetClient != null)
        {
            connected = telnetClient.isConnected();
        }
        return connected;
    }

    public String waitFor(String[] waitForArray) throws IOException
    {
        InputStreamReader responseReader = null;
        StringBuffer answerFromRemoteHost = new StringBuffer();
        try
        {
            responseReader = new InputStreamReader(telnetClient.getInputStream());
            boolean found = false;
            do
            {
                char readChar = 0;
                long currentTime = System.currentTimeMillis();
                long timeoutTime = currentTime + timeout;
                while (readChar == 0)
                {
                    if (responseReader == null)
                    {
                        // responseReader can only be set to null if method
                        // releaseTelnetInputStreamReader()
                        // has been called, which should happen if host becomes
                        // unavailable.
                        throw new IOException(
                                "Telnet host is unavailable; stopped waiting for answer.");
                    }
                    if (responseReader.ready())
                    {
                        readChar = (char) responseReader.read();
                    }
                    else
                    {
                        try
                        {
                            Thread.sleep(50);
                        }
                        catch (InterruptedException e)
                        {
                            // Do nothing
                        }
                    }
                    currentTime = System.currentTimeMillis();
                    if ((!responseReader.ready()) && (currentTime > timeoutTime))
                    {
                        throw new IOException(
                                "A timeout has occured when trying to read the telnet stream");
                    }
                }
                answerFromRemoteHost.append(readChar);
                for (String aWaitFor : waitForArray)
                {
                    found = answerFromRemoteHost.toString().contains(aWaitFor);
                }
            } while (!found);
        }
        finally
        {
            if (responseReader != null)
            {
                responseReader.close();
            }
        }
        return answerFromRemoteHost.toString();
    }


    public InputStream getInputStream()
    {
        InputStream s = null;
        if (telnetClient != null)
        {
            s = telnetClient.getInputStream();
        }
        return s;
    }

    public OutputStream getOutputStream()
    {
        OutputStream s = null;
        if (telnetClient != null)
        {
            s = telnetClient.getOutputStream();
        }
        return s;
    }
}