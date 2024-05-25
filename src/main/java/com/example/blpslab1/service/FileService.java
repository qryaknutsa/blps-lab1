package com.example.blpslab1.service;

import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.dto.MessageNode;
import com.example.blpslab1.dto.MessageRequest;
import com.example.blpslab1.dto.MessageResponse;
import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.FileResponse;
import com.example.blpslab1.model.RabbitNode;

import com.example.blpslab1.utils.JackRabbitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import jakarta.ws.rs.NotFoundException;
import java.io.*;
import java.util.*;

import static com.example.blpslab1.subModel.FileType.*;

@Service
@Slf4j
@Component
@EnableJms
public class FileService {

    private final JmsTemplate jmsTemplate;
    private final OwnershipService ownershipService;
    private final UserService userService;


    @Autowired
    public FileService(JmsTemplate jmsTemplate, OwnershipService ownershipService, UserService userService) {
        this.jmsTemplate = jmsTemplate;
        this.ownershipService = ownershipService;
        this.userService = userService;
    }

    Logger logger = LoggerFactory.getLogger(FileService.class);

    @Transactional
    public Node createNode(Session session, RabbitNode input, MultipartFile uploadFile) {
        Node node = null;
        File file = new File(Objects.requireNonNull(uploadFile.getOriginalFilename()));

        try {
            Node parentNode = session.getNodeByIdentifier(input.getParentId());
            if (parentNode != null && parentNode.hasNode(file.getName())) {
                logger.error(file.getName() + " node already exists!");
                return editNode(session, input, uploadFile);
            } else {
                try {
                    node = parentNode.addNode(file.getName(), "nt:file");
                    node.addMixin("mix:versionable");
                    node.addMixin("mix:referenceable");

                    Node content = node.addNode("jcr:content", "nt:resource");

                    InputStream inputStream = uploadFile.getInputStream();
                    Binary binary = session.getValueFactory().createBinary(inputStream);

                    content.setProperty("jcr:data", binary);
                    content.setProperty("jcr:mimeType", input.getMimeType());

                    Date now = new Date();
                    now.toInstant().toString();
                    content.setProperty("jcr:lastModified", now.toInstant().toString());

                    inputStream.close();
                    session.save();

                    VersionManager vm = session.getWorkspace().getVersionManager();
                    vm.checkin(node.getPath());

                    logger.info("File saved!");
                } catch (Exception e) {
                    logger.error("Exception caught!");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }
        return node;
    }

    @Transactional
    public boolean deleteNode(Session session, RabbitNode input) {
        try {
            Node node = session.getNodeByIdentifier(input.getFileId());
            if (node != null) {
                node.remove();
                session.save();
                return true;
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }

        return false;
    }

    public List<String> getVersionHistory(Session session, RabbitNode input) {
        List<String> versions = new ArrayList<>();
        try {
            VersionManager vm = session.getWorkspace().getVersionManager();

            Node node = session.getNodeByIdentifier(input.getFileId());
            String filePath = node.getPath();
            if (session.itemExists(filePath)) {
                VersionHistory versionHistory = vm.getVersionHistory(filePath);
                Version currentVersion = vm.getBaseVersion(filePath);
                logger.info("Current version: " + currentVersion.getName());

                VersionIterator versionIterator = versionHistory.getAllVersions();
                while (versionIterator.hasNext()) {
                    versions.add(((Version) versionIterator.next()).getName());
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }
        return versions;
    }

    @Transactional
    public Node editNode(Session session, RabbitNode input, MultipartFile uploadFile) {
        File file = new File(uploadFile.getOriginalFilename());
        Node returnNode = null;

        try {
            Node parentNode = session.getNodeByIdentifier(input.getParentId());
            if (parentNode != null && parentNode.hasNode(file.getName())) {
                VersionManager vm = session.getWorkspace().getVersionManager();

                Node fileNode = parentNode.getNode(file.getName());
                vm.checkout(fileNode.getPath());

                Node content = fileNode.getNode("jcr:content");

                InputStream is = uploadFile.getInputStream();
                Binary binary = session.getValueFactory().createBinary(is);
                content.setProperty("jcr:data", binary);

                session.save();
                is.close();

                vm.checkin(fileNode.getPath());
                returnNode = fileNode;
            }
        } catch (Exception e) {
            logger.error("Exception caught");
            e.printStackTrace();
        }

        return returnNode;
    }


    @Transactional
    public Node createFolderNode(Session session, RabbitNode input) {
        Node node = null;
        Node parentNode = null;

        try {
            parentNode = session.getNodeByIdentifier(input.getParentId());
            if (session.nodeExists(parentNode.getPath())) {
                if (!parentNode.hasNode(input.getFileName())) {
                    node = parentNode.addNode(input.getFileName(), "nt:folder");
                    node.addMixin("mix:referenceable");
                    session.save();
                    System.out.println("Folder created: " + input.getFileName());
                }
            } else {
                logger.error("Node already exists!");
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }

        return node;
    }

    public FileResponse getNode(Session session, String versionId, RabbitNode input) {
        FileResponse response = new FileResponse();

        try {
            Node file = session.getNodeByIdentifier(input.getFileId());
            if (file != null) {
                VersionManager vm = session.getWorkspace().getVersionManager();
                VersionHistory history = vm.getVersionHistory(file.getPath());
                for (VersionIterator it = history.getAllVersions(); it.hasNext(); ) {
                    Version version = (Version) it.next();
                    if (versionId.equals(version.getName())) {
                        file = version.getFrozenNode();
                        break;
                    }
                }

                logger.info("Node retrieved: " + file.getPath());

                Node fileContent = file.getNode("jcr:content");
                Binary bin = fileContent.getProperty("jcr:data").getBinary();
                InputStream stream = bin.getStream();
                byte[] bytes = IOUtils.toByteArray(stream);
                bin.dispose();
                stream.close();

                response.setBytes(bytes);
                //  response.setContentType(fileContent.getProperty("jcr:mimeType").getString());
                response.setContentType(input.getMimeType());
                return response;

            } else {
                logger.error("Node does not exist!");
            }

        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }
        return response;
    }


    @JmsListener(destination = "CopyDir-request")
    @Transactional
    public void pasteDir(MessageRequest request) {
        MessageResponse response = new MessageResponse();

        String username = request.getUsername();
        String targetDir = request.getTargetDir();
        ArrayList<MessageNode> list = request.getList();

        Repository repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);


        try {
            userService.getUser(username);
            ownershipService.getRecord(username, targetDir);

            Session session = JackRabbitUtils.getSession(repo);

            assert session != null;
            setDir(session, targetDir, list, username);

            JackRabbitUtils.cleanUp(session);
            response.setMessage("Папка скопирована");
        } catch (UserNotFoundException e) {
            response.setMessage("Такого пользователя нет.");
        } catch (NotFoundException e) {
            response.setMessage("У пользователя нет доступа к этой директории.");
        } catch (Exception e) {
            response.setMessage("Что то пошло не так ;( " + e.getMessage());
            System.out.println(e.getMessage());
        }

        jmsTemplate.convertAndSend("CopyDir-response", response);

    }


    int megaIt = 1;

    @Transactional
    public void setDir(Session session, String targetDir, ArrayList<MessageNode> list, String username) {
        try {
            Node targetParent = session.getNodeByIdentifier(targetDir);
            Node targetFolder = targetParent.addNode(list.getFirst().getName(), "nt:folder");
            targetFolder.addMixin("mix:referenceable");

            ownershipService.addRecord(username, targetFolder.getIdentifier(), FOLDER, targetFolder.getName());

            for (; megaIt < list.size(); megaIt++) {
                MessageNode node = list.get(megaIt);
                if (node.getFileType() == FOLDER) {
                    megaIt++;
                    setDir(session, node.getIdentifier(), list, username);
                } else if (node.getFileType() == FILE) {
                    Node file = targetFolder.addNode(node.getName(), "nt:file");
                    file.addMixin("mix:versionable");
                    file.addMixin("mix:referenceable");
                    ownershipService.addRecord(username, file.getIdentifier(), FILE, file.getName());
                    Node content = file.addNode("jcr:content", "nt:resource");
                    content.setProperty("jcr:data", convertBinary(node.getData()));
                    Date now = new Date();
                    content.setProperty("jcr:lastModified", now.toInstant().toString());
                }
            }
            session.save();
            System.out.println("Folder copied");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public Binary convertBinary(org.bson.types.Binary bsonBinary) {
        byte[] data = bsonBinary.getData();

        return new Binary() {
            @Override
            public InputStream getStream() throws RepositoryException {
                return new ByteArrayInputStream(data);
            }

            @Override
            public int read(byte[] b, long position) throws IOException, RepositoryException {
                // Метод read должен быть реализован в соответствии с вашими потребностями
                // Он должен читать данные из массива байтов и возвращать количество прочитанных байтов
                // position - позиция, с которой нужно начать чтение
                // b - массив байтов, в который нужно записать данные
                // В данном примере просто копируем все данные из массива data в массив b
                System.arraycopy(data, (int) position, b, 0, data.length - (int) position);
                return data.length - (int) position;
            }

            @Override
            public long getSize() throws RepositoryException {
                return data.length;
            }

            @Override
            public void dispose() {
                // Метод dispose может быть реализован по вашему усмотрению
                // Он может освобождать ресурсы, связанные с данными
            }
        };
    }

}
