/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package io.mycat.net2.mysql.response;

import java.nio.ByteBuffer;

import io.mycat.net2.mysql.connection.front.MySQLFrontendConnection;
import io.mycat.net2.mysql.definination.Fields;
import io.mycat.net2.mysql.packet.EOFPacket;
import io.mycat.net2.mysql.packet.FieldPacket;
import io.mycat.net2.mysql.packet.MySQLPacket;
import io.mycat.net2.mysql.packet.ResultSetHeaderPacket;
import io.mycat.net2.mysql.packet.RowDataPacket;
import io.mycat.net2.mysql.packet.util.PacketUtil;

/**
 * @author mycat
 */
public final class SelectVersionComment {

    private static final byte[] VERSION_COMMENT = "MyCat Server (monitor)".getBytes();
    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("@@VERSION_COMMENT", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void response(MySQLFrontendConnection c) {
    	ByteBuffer byteBuf= c.getWriteDataBuffer().beginWrite(1024);
        // write header
        header.write(byteBuf,header.calcPacketSize());

        // write fields
        for (FieldPacket field : fields) {
            field.write(byteBuf,field.calcPacketSize());

        }

        // write eof
        eof.write(byteBuf,eof.calcPacketSize());

        // write rows
        byte packetId = eof.packetId;
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(VERSION_COMMENT);
        row.packetId = ++packetId;
        row.write(byteBuf,row.calcPacketSize());

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        lastEof.write(byteBuf,lastEof.calcPacketSize());

        // post write
        c.getWriteDataBuffer().endWrite(byteBuf);
        c.enableWrite(true);

        c.setNextConnectedStatus(MySQLPacket.OK_PACKET);
        c.setNextConnectedStatus(MySQLPacket.OK_PACKET);
        c.setNextConnectedStatus(MySQLPacket.EOF_PACKET);
    }

}