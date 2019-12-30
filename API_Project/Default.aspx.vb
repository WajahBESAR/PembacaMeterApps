Imports System.IO
Imports System.Data.SqlClient
Imports System.Net
Imports System.Drawing

Partial Class _Default
    Inherits System.Web.UI.Page
    Dim DBUser, DBPass, DBHost, DBName, ConnectionString As String
    Dim URLsvc As String
    Dim picHost As String

    Protected Sub Page_Load(sender As Object, e As EventArgs) Handles Me.Load

        Try
            Using sr As StreamReader = New StreamReader(Server.MapPath("config.ini"))
                Dim line As String
                Do
                    line = sr.ReadLine
                    If Not (line Is Nothing) Then
                        If Mid(line, 1, 3) = "dbu" Then
                            DBUser = Mid(line, 5)
                        End If
                        If Mid(line, 1, 3) = "dbp" Then
                            DBPass = Mid(line, 5)
                        End If
                        If Mid(line, 1, 3) = "dbh" Then
                            DBHost = Mid(line, 5)
                        End If
                        If Mid(line, 1, 3) = "dbn" Then
                            DBName = Mid(line, 5)
                        End If
                        If Mid(line, 1, 3) = "url" Then
                            URLsvc = Mid(line, 5)
                        End If
                        If Mid(line, 1, 3) = "pic" Then
                            picHost = Mid(line, 5)
                        End If

                        ConnectionString = "Server=" & DBHost & ";Database=" & DBName & ";User Id=" & DBUser & ";Password=" & DBPass & ";MultipleActiveResultSets=True"

                    End If
                Loop Until line Is Nothing
            End Using
        Catch exp As Exception
            Response.Write("database configuration file error!<br>" & exp.Message)
            Exit Sub
        End Try

        Dim postedToken As String = Request("token") & ""
        If validToken(postedToken) = "invalid-token" Then
            Response.Write("invalid-token")
            Exit Sub
        End If

        Dim ACTION = Request("action") & ""

        Select Case ACTION
            Case "ping"
                Response.Write("pong")
            Case "login"
                Dim ini As String = Request("init") & ""
                Dim logid As String = Request("logid") & ""
                Dim lat As String = Request("lat") & ""
                Dim lng As String = Request("lng") & ""
                Response.Write(DoLogin(ini, logid, lat, lng))
            Case "kick"
                Dim ini As String = Request("init") & ""
                Response.Write(loginKick(ini))
            Case "logout"
                Dim ini As String = Request("init") & ""
                Dim logid As String = Request("logid") & ""
                Dim lat As String = Request("lat") & ""
                Dim lng As String = Request("lng") & ""
                Response.Write(doLogout(ini, logid, lat, lng))
            Case "download"
                Dim ini As String = Request("init") & ""
                Dim logid As String = Request("logid") & ""
                Response.Write(DownloadForMobile(ini, logid))
            Case "notes"
                Response.Write(DownloadCatatan())
            Case "history"
                Response.Write(HistoryBacaan())
            Case "logger"
                Dim init As String = Request("init") & ""
                Dim lat As String = Request("lat") & ""
                Dim lon As String = Request("lon") & ""
                Dim act As String
                If Request("act") <> "" Then
                    act = Request("act").Trim
                Else
                    act = ""
                End If
                saveLog(init, lat, lon, act)
            Case "housepic"
                Dim nopel As String = Request("custid") & ""
                Response.Write(getHousePicture(nopel))
            Case "datetime"
                Response.Write(getDateTime())
            Case "upload"
                Dim init As String = Request("init") & ""
                Dim readday As String = Request("readday") & ""
                Dim custid As String = Request("custid") & ""
                Dim reqyear As String = Request("year") & ""
                Dim reqmonth As String = Request("month") & ""
                Dim reqdate As String = Request("date") & ""
                Dim stand As String = Request("stand") & ""
                Dim notes As String = Request("notes") & ""
                Dim desc As String = Request("desc") & ""
                Dim fulldate As String = Request("fulldate") & ""

                Dim FILEUPLOAD As HttpPostedFile = Request.Files("photometer")
                Response.Write(UploadPhotoMeter(init, readday, custid, reqyear, reqmonth, reqdate, stand, notes, desc, fulldate, FILEUPLOAD))
            Case "getnotes" ' dilakukan via web ui
                Response.Write(getNotes())
            Case "getdata" ' dilakukan via web ui
                Dim init As String = Request("init") & ""
                Response.Write(getData(init))
            Case Else
                Response.Write("invalid-action")
        End Select
    End Sub

    Function UploadPhotoMeter(ByVal init As String, ByVal readday As String, ByVal custid As String, ByVal reqyear As String, ByVal reqmonth As String, ByVal reqdate As String, ByVal stand As String, ByVal notes As String, ByVal desc As String, ByRef fulldate As String, ByVal photometer As HttpPostedFile) As String
        Dim RESULT As String = ""
        If photometer IsNot Nothing AndAlso photometer.ContentLength Then
            Try
                ' Save data
                Using CON As New SqlConnection(ConnectionString)
                    CON.Open()
                    Dim SQL As String = "INSERT INTO tBacaan (" & _
                        "fBacaan_Init, fBacaan_Haribaca, fBacaan_Nopel, fBacaan_Tahun, fBacaan_Bulan, fBacaan_Hari, fBacaan_Stand, fBacaan_Catatan, fBacaan_Keterangan, fBacaan_Tanggal" & _
                        ") VALUES (" & _
                        "@INIT, @HARIBACA, @NOPEL, @TAHUN, @BULAN, @HARI, @STAND, @CATATAN, @KETERANGAN, @TANGGAL" & _
                        ")"
                    Dim CMD As New SqlCommand(SQL, CON)
                    CMD.Parameters.AddWithValue("@INIT", init.Trim)
                    CMD.Parameters.AddWithValue("@HARIBACA", readday.Trim)
                    CMD.Parameters.AddWithValue("@NOPEL", custid.Trim)
                    CMD.Parameters.AddWithValue("@TAHUN", reqyear.Trim)
                    CMD.Parameters.AddWithValue("@BULAN", reqmonth.Trim)
                    CMD.Parameters.AddWithValue("@HARI", reqdate.Trim)
                    CMD.Parameters.AddWithValue("@STAND", stand.Trim)
                    CMD.Parameters.AddWithValue("@CATATAN", notes.Trim)
                    CMD.Parameters.AddWithValue("@KETERANGAN", desc.Trim)
                    CMD.Parameters.AddWithValue("@TANGGAL", fulldate)
                    CMD.ExecuteNonQuery()
                    CON.Close()
                    RESULT = "ok"
                End Using

                ' Save photo
                If RESULT = "ok" Then
                    Directory.CreateDirectory(Server.MapPath("PhotoMeter"))
                    Dim PhotoPath As String = (Server.MapPath("PhotoMeter") & "\") + photometer.FileName
                    photometer.SaveAs(PhotoPath)
                End If

            Catch ex As Exception
                Return "upload-error"
            End Try
        Else
            Return "upload-empty-file"
        End If

        Return RESULT
    End Function

    Function getDateTime() As String
        Dim DT As DateTime = DateTime.Now
        Dim HariEN As String = Now.DayOfWeek
        Dim HariID(7) As String
        HariID(0) = "Minggu"
        HariID(1) = "Senin"
        HariID(2) = "Selasa"
        HariID(3) = "Rabu"
        HariID(4) = "Kamis"
        HariID(5) = "Jumat"
        HariID(6) = "Sabtu"

        Return DT.ToString("yyyy-MM-dd hh.mm.ss") & " " & HariID(HariEN)
    End Function

    Function getHousePicture(ByVal strNopel As String) As String
        Dim RESULT As String = ""
        If strNopel.Length = 8 Then
            Dim PhotoPath As String = picHost & "/PhotoRumah/" & strNopel.Substring(0, 4) & "-" & strNopel.Substring(4, 4) & ".JPG"
            Dim tClient As WebClient = New WebClient
            Try
                ' Tangkap Bitmap dari server 
                Dim tImage As Bitmap = Bitmap.FromStream(New MemoryStream(tClient.DownloadData(PhotoPath)))
                'Dim tempImage As Bitmap = New Bitmap(tImage, 75, 75)

                ' simpan sebagai file jpg
                Dim ms As New MemoryStream
                tImage.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg) ' Asli ga dikecilin
                'tempImage.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg) ' dikecilin 75%

                'tampilkan di layar
                'tempImage.Save(Response.OutputStream, System.Drawing.Imaging.ImageFormat.Gif)
                'Response.ContentType = "image/jpeg"

                '' urai jpg menjadi base64 string
                Dim bytes() As Byte = ms.ToArray
                Dim image_base64String As String = Convert.ToBase64String(bytes)

                ' menghasilkan base64 string
                RESULT = image_base64String
            Catch
                RESULT = ""
            End Try
        Else
            RESULT = ""
        End If

        Return RESULT
    End Function

    Sub saveLog(ByVal init As String, ByVal lat As String, ByVal lon As String, ByVal act As String)
        Using CON As New SqlConnection(ConnectionString)
            CON.Open()
            Dim CMD As New SqlCommand("INSERT INTO tLog (fLog_Initial, fLog_Datetime, fLog_Latitude, fLog_Longitude, fLog_Activity) VALUES (@INI, @DAT, @LAT, @LON, @ACT)", CON)
            CMD.Parameters.AddWithValue("@INI", init.Trim)
            CMD.Parameters.AddWithValue("@DAT", Now)
            CMD.Parameters.AddWithValue("@LAT", lat.Trim)
            CMD.Parameters.AddWithValue("@LON", lon.Trim)
            CMD.Parameters.AddWithValue("@ACT", act)
            CMD.ExecuteNonQuery()
            CON.Close()
        End Using
    End Sub

    Function HistoryBacaan() As String
        Dim RESULT As String = ""

        RESULT = "Data tidak tersedia!"
        Return RESULT
    End Function

    Function DownloadCatatan() As String
        Dim RESULT As String = ""

        Try
            Using CON As New SqlConnection(ConnectionString)
                CON.Open()
                Dim CMD As New SqlCommand("SELECT * FROM tCatatan ORDER BY fCatatan_Kode ASC", CON)
                Dim REC As SqlDataReader = CMD.ExecuteReader
                If REC.HasRows Then
                    While REC.Read
                        RESULT = RESULT & REC("fCatatan_Kode").ToString.Trim & "," & REC("fCatatan_Keterangan").ToString.Trim & ";"
                    End While
                    If RESULT <> "" Then
                        RESULT = RESULT.Substring(0, RESULT.Length - 1)
                    Else
                        RESULT = "mt_error"
                    End If
                Else
                    RESULT = "null_error"
                End If
                REC.Close()
                CON.Close()
            End Using
        Catch ex As Exception
            RESULT = "exp_err"
        End Try
        Return RESULT
    End Function

    Function getNotes() As String
        Dim RESULT As String = ""
        Dim webRequest As WebRequest
        Dim webresponse As WebResponse
        Dim inStream As StreamReader
        Dim strRes As String = ""

        Try
            webRequest = webRequest.Create(URLsvc & "/?p=cat") ' url + querystring
            webresponse = webRequest.GetResponse()
            inStream = New StreamReader(webresponse.GetResponseStream())
            strRes = inStream.ReadToEnd

            Try
                Using CON As New SqlConnection(ConnectionString)
                    CON.Open()

                    '  DBCC CHECKIDENT('tPelanggan', RESEED, 0) ' SQLServer Command untuk reset autoincrement menjadi 1

                    ' kosongkan data catatan
                    Dim CMD As New SqlCommand("DELETE FROM tCatatan", CON)
                    CMD.ExecuteNonQuery()

                    ' looping di file text hasil dari request cis
                    Dim arrResult() As String = strRes.Split(";")
                    For Each row As String In arrResult
                        Dim baris() As String = row.Split("|")
                        CMD.CommandText = "INSERT INTO tCatatan (fCatatan_Kode, fCatatan_Keterangan) VALUES ('" & Convert.ToInt32(baris(0).Trim) & "', '" & baris(1).Trim & "')"
                        CMD.Connection = CON

                        CMD.ExecuteNonQuery()
                    Next
                    CON.Close()
                    RESULT = "ok"
                End Using
            Catch e As Exception
                RESULT = e.Message
            End Try

        Catch ex As Exception
            RESULT = ex.Message
        End Try

        Return RESULT
    End Function

    Function DownloadForMobile(ByVal initial As String, ByVal loginid As String) As String
        Dim RESULT As String = ""

        If cekLogin(initial, loginid) = True Then
            Try
                Dim tgl As Integer
                Dim hari As String
                tgl = Date.Today.Day
                hari = tgl - 1
                Using CON As New SqlConnection(ConnectionString)
                    CON.Open()
                    Dim CMD As New SqlCommand("SELECT * FROM tPelanggan WHERE fPelanggan_ReadInit = @INIT AND fPelanggan_ReadDay = @DAY AND fPelanggan_Sync = 0 ORDER BY fPelanggan_Nopel ASC", CON)
                    CMD.Parameters.AddWithValue("@INIT", initial)
                    CMD.Parameters.AddWithValue("@DAY", hari)
                    Dim REC As SqlDataReader = CMD.ExecuteReader
                    If REC.HasRows Then
                        While REC.Read
                            RESULT = RESULT & "" & REC("fPelanggan_Nopel").ToString.Trim & "," & REC("fPelanggan_Nama").ToString.Trim & "," & REC("fPelanggan_Alamat").ToString.Trim & "," & REC("fPelanggan_Goltar").ToString.Trim & _
                                "," & REC("fPelanggan_Telp").ToString.Trim & "," & REC("fPelanggan_Metnum").ToString.Trim & "," & REC("fPelanggan_Lat").ToString.Trim & "," & REC("fPelanggan_Lng") & ";"
                        End While
                    End If
                    REC.Close()
                    CON.Close()

                    If RESULT = "" Then
                        RESULT = "empty"
                    Else
                        RESULT = RESULT.Substring(0, RESULT.Length - 1)
                    End If
                End Using
            Catch ex As Exception

            End Try
        Else
            RESULT = "err"
        End If

        Return RESULT
    End Function

    Function getData(ByVal init As String) As String
        Dim RESULT As String = ""
        Dim resCIS As String = ""
        Dim tgl As Integer
        Dim hari As String
        tgl = Date.Today.Day
        hari = tgl - 1

        Try
            Dim inStream As StreamReader
            Dim webRequest As WebRequest
            Dim webresponse As WebResponse
            'Dim host As String = HttpContext.Current.Request.Url.Scheme & "://" & HttpContext.Current.Request.Url.Authority & "/" & URLsvc & "/?p=baca&ini=" & init
            webRequest = webRequest.Create(URLsvc & "/?p=baca&ini=" & init) ' url + querystring
            webresponse = webRequest.GetResponse()
            inStream = New StreamReader(webresponse.GetResponseStream())
            resCIS = inStream.ReadToEnd

            Try
                Using CON As New SqlConnection(ConnectionString)
                    CON.Open()

                    ' minta ke farhan. api untuk cari jumlah pelanggan per init per haribaca (hari ini) semua (yang dibaca dan yang belum) -> ?p=jml&ini=JOK -> 300
                    ' kosongkan data untuk init dan haribaca ini
                    Dim CMD As New SqlCommand("DELETE FROM tPelanggan WHERE fPelanggan_ReadInit = @INIT AND fPelanggan_ReadDay = @DAY", CON)
                    CMD.Parameters.AddWithValue("@INIT", init)
                    CMD.Parameters.AddWithValue("@DAY", hari)
                    CMD.ExecuteNonQuery()

                    ' looping di file text hasil dari request cis
                    Dim arrResCIS() As String = resCIS.Split(";")
                    For Each row As String In arrResCIS
                        '1|1045-1047|NY SUTARTO|JL MENTENG KULON|BLK.7-27|1|2|R2||078263|-6.58681454|106.78466899
                        Dim arrRowCis() As String = row.Split("|")
                        Dim fNopel As String = arrRowCis(1).Trim.Substring(0, 9)
                        Dim fNama As String = arrRowCis(2).Trim.PadRight(25).Substring(0, 25)
                        Dim fAlamat As String = arrRowCis(3).Trim & " " & arrRowCis(4).Trim & " RT/RW " & arrRowCis(5).Trim & "/" & arrRowCis(6).Trim
                        fAlamat = fAlamat.PadRight(100).Substring(0, 100)
                        Dim fGoltar As String = arrRowCis(7).Trim.PadRight(2).Substring(0, 2)
                        Dim fMetnum As String = arrRowCis(9).Trim.PadRight(30).Substring(0, 30)
                        Dim fTelp As String = arrRowCis(8).Trim.PadRight(50).Substring(0, 50)
                        Dim fLat As String = arrRowCis(10).Trim.PadRight(50).Substring(0, 50)
                        Dim fLng As String = arrRowCis(11).Trim.PadRight(50).Substring(0, 50)
                        Dim fInisial As String = init.PadRight(3).Substring(0, 3)
                        Dim fHari As String = hari.PadRight(2).Substring(0, 2)
                        Dim fSync As Integer = 0

                        '  DBCC CHECKIDENT('tPelanggan', RESEED, 0) ' SQLServer Command untuk reset autoincrement menjadi 1
                        CMD.CommandText = "INSERT INTO tPelanggan (fPelanggan_Nopel, fPelanggan_Nama, fPelanggan_Alamat, fPelanggan_Goltar, fPelanggan_Metnum, fPelanggan_Telp, fPelanggan_Lat, fPelanggan_Lng, fPelanggan_ReadInit, fPelanggan_ReadDay, fPelanggan_Sync) " & _
                            "VALUES ('" & fNopel & "', '" & fNama & "', '" & fAlamat & "', '" & fGoltar & "', '" & fMetnum & "', '" & fTelp & "', '" & fLat & "', '" & fLng & "', '" & fInisial & "', '" & fHari & "', 0)"
                        CMD.Connection = CON

                        CMD.ExecuteNonQuery()
                    Next
                    CON.Close()
                    RESULT = "ok"
                End Using
            Catch ex As Exception
                RESULT = ex.Message
            End Try

        Catch ex As Exception
            RESULT = ex.Message
        End Try

        Return RESULT
    End Function

    Function doLogout(ByVal init As String, ByVal logid As String, ByVal lat As String, ByVal lng As String) As String
        Dim RESULT As String = ""

        Try
            Using CON As New SqlConnection(ConnectionString)
                CON.Open()
                Dim CMD As New SqlCommand("UPDATE tPetugas SET fPetugas_Login = '', fPetugas_Lat = @LAT, fPetugas_Lng = @LNG, fPetugas_Date = @DAT WHERE fPetugas_Init = @INI", CON)
                CMD.Parameters.AddWithValue("@LAT", lat.Trim)
                CMD.Parameters.AddWithValue("@LNG", lng.Trim)
                CMD.Parameters.AddWithValue("@DAT", Now())
                CMD.Parameters.AddWithValue("@INI", init.Trim)
                CMD.ExecuteNonQuery()
                CON.Close()
                RESULT = "ok"
            End Using
        Catch ex As Exception
            RESULT = "err"
        End Try

        Return RESULT
    End Function

    Function loginKick(ByVal initial As String) As String
        Dim RESULT As String = ""

        Try
            Using CON As New SqlConnection(ConnectionString)
                CON.Open()
                Dim CMD As New SqlCommand("UPDATE tPetugas SET fPetugas_Login = '' WHERE fPetugas_Init = @INIT", CON)
                CMD.Parameters.AddWithValue("@INIT", initial)
                CMD.ExecuteNonQuery()
                CON.Close()
                RESULT = "ok"
            End Using
        Catch ex As Exception
            RESULT = ex.Message
        End Try

        Return RESULT
    End Function

    Function DoLogin(ByVal initial As String, ByVal loginid As String, ByVal lat As String, ByVal lng As String) As String
        Dim RESULT As String = ""
        Dim isFail As Boolean
        Dim DT As DateTime = DateTime.Now
        Dim HariEN As String = Now.DayOfWeek
        Dim HariID(7) As String
        HariID(0) = "Minggu"
        HariID(1) = "Senin"
        HariID(2) = "Selasa"
        HariID(3) = "Rabu"
        HariID(4) = "Kamis"
        HariID(5) = "Jumat"
        HariID(6) = "Sabtu"

        Try
            Using CON As New SqlConnection(ConnectionString)
                CON.Open()
                Dim CMD As New SqlCommand("SELECT fPetugas_Init, fPetugas_Nama, fPetugas_Avatar, fPetugas_Login FROM tPetugas WHERE fPetugas_Init = @INIT", CON)
                CMD.Parameters.AddWithValue("@INIT", initial)
                Dim REC As SqlDataReader = CMD.ExecuteReader
                Dim strRes As String = ""
                Dim hari As Integer
                hari = Date.Today.Day

                If REC.HasRows Then
                    While REC.Read
                        ' Nama file foto -> NAMAFILE.JPG aja
                        If (REC(3).ToString.Trim.Length < 1) Then
                            strRes = REC(0).ToString.Trim & "," & REC(1).ToString.Trim & "," & hari - 1 & "," & "/avatar/" & REC(2).ToString.Trim & "," & DT.ToString("yyyy-MM-dd hh.mm.ss") & " " & HariID(HariEN)
                            isFail = False
                        Else
                            If (REC(3).ToString.Trim() = loginid) Then
                                strRes = REC(0).ToString.Trim & "," & REC(1).ToString.Trim & "," & hari - 1 & "," & "/avatar/" & REC(2).ToString.Trim & "," & DT.ToString("yyyy-MM-dd hh.mm.ss") & " " & HariID(HariEN)
                                isFail = False
                            Else
                                isFail = True
                            End If
                        End If
                    End While
                Else
                    RESULT = "not-found"
                End If
                REC.Close()

                If isFail = False Then
                    CMD.CommandText = "UPDATE tPetugas SET fPetugas_Login = @LOGID, fPetugas_Lat = @LOGLAT, fPetugas_Lng = @LOGLNG, fPetugas_Date = @LOGDATE WHERE fPetugas_Init = @LOGINIT"
                    CMD.Parameters.AddWithValue("@LOGID", loginid)
                    CMD.Parameters.AddWithValue("@LOGLAT", lat)
                    CMD.Parameters.AddWithValue("@LOGLNG", lng)
                    CMD.Parameters.AddWithValue("@LOGDATE", Now())
                    CMD.Parameters.AddWithValue("@LOGINIT", initial)
                    CMD.Connection = CON
                    CMD.ExecuteNonQuery()
                    RESULT = "ok," & strRes
                Else
                    RESULT = "already-login"
                End If
                CON.Close()
            End Using
        Catch ex As Exception
            RESULT = ex.Message
        End Try

        Return RESULT
    End Function

    Function cekLogin(ByVal initial As String, ByVal loginid As String) As Boolean
        Dim RESULT As String = ""

        Try
            Using CON As New SqlConnection(ConnectionString)
                CON.Open()
                Dim CMD As New SqlCommand("SELECT fPetugas_ID FROM tPetugas WHERE fPetugas_Init = @INIT AND fPetugas_Login = @LOGID", CON)
                CMD.Parameters.AddWithValue("@INIT", initial)
                CMD.Parameters.AddWithValue("@LOGID", loginid)
                Dim REC As SqlDataReader = CMD.ExecuteReader
                If REC.HasRows Then
                    While REC.Read
                        RESULT = REC(0).ToString.Trim
                    End While
                End If
                REC.Close()
                CON.Close()
            End Using
        Catch ex As Exception
        End Try

        If RESULT = "" Then
            Return False
        Else
            Return True
        End If
    End Function

    Function validToken(ByVal postedToken As String) As String
        Dim RESULT As String = ""

        Try
            Using CON As New SqlConnection(ConnectionString)
                CON.Open()
                Dim CMD As New SqlCommand("SELECT * FROM tToken WHERE fToken_Token = @TOKEN", CON)
                CMD.Parameters.AddWithValue("@TOKEN", postedToken)
                Dim REC As SqlDataReader = CMD.ExecuteReader
                If REC.HasRows Then
                    While REC.Read
                        RESULT = REC("fToken_Name").ToString.Trim
                    End While
                End If
                REC.Close()
                CON.Close()
            End Using
            If RESULT = "" Then RESULT = "invalid-token"
        Catch e As Exception
            RESULT = e.Message
        End Try

        Return RESULT

    End Function
End Class
